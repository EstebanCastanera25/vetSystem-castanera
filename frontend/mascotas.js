// ============================================================================
//  Mascotas — lo propio de esta página.
//
//  Todo lo que es igual en las cuatro páginas (pedir a la API, avisos, buscador,
//  orden, panel lateral) vive en app.js, que se carga antes que este archivo.
// ============================================================================

// URL_BASE la define app.js. URL_API la define CADA página: si app.js también la
// declarara, serían dos const con el mismo nombre y el navegador no ejecutaría nada.
const URL_API = URL_BASE + "/mascotas";
const URL_DUENIOS = URL_BASE + "/duenios";

let idEnEdicion = null;
let modal = null;
let listaActual = [];


// ----------------------------------------------------------------------------
//  Tabla
// ----------------------------------------------------------------------------

async function cargarTabla(texto) {
    const buscado = (texto === undefined || texto === null) ? "" : texto;
    const numeroPedido = nuevoPedido();
    mostrarSpinnerTabla(true);
    try {
        const respuesta = await pedir(urlDeListado(URL_API, "buscar", buscado));

        // Si mientras esperábamos salió otro pedido (otra tecla, otra página, otro
        // orden), éste ya quedó viejo: se descarta para no pisar al nuevo.
        if (!pedidoVigente(numeroPedido)) {
            return;
        }
        listaActual = leerPagina(respuesta);

        // Si la página pedida quedó vacía (borraste el último registro de la última),
        // se retrocede a la última que sí existe y se vuelve a pedir.
        if (corregirPaginaVacia()) {
            await cargarTabla(buscado);
            return;
        }
        redibujarTabla();
    } catch (error) {
        if (!pedidoVigente(numeroPedido)) {
            return;
        }
        listaActual = [];
        vaciarPaginacion();
        redibujarTabla();
        mostrarAviso(error.message);
    } finally {
        // Sólo el pedido más nuevo apaga el spinner: si no, uno viejo lo apagaría
        // mientras el nuevo todavía está viajando.
        if (pedidoVigente(numeroPedido)) {
            mostrarSpinnerTabla(false);
        }
    }
}

// El filtro que está escrito AHORA en la pantalla. Se lee del DOM y no de una variable
// para que no puedan quedar desincronizados: lo que se ve es la verdad.
function filtroDeLaPagina() {
    return document.getElementById("campoBuscar").value.trim();
}

// Vuelve a pedir la página actual con el filtro actual. La usan el orden, el paginador,
// el selector de tamaño, guardar() y eliminar().
function recargarTabla() {
    return cargarTabla(filtroDeLaPagina());
}

// Dibuja las filas que ya están en memoria y, al final, el paginador.
function redibujarTabla() {
    vaciarTabla();
    // Se recorre con índice porque la columna "#" muestra el número de fila
    for (let i = 0; i < listaActual.length; i = i + 1) {
        agregarFila(listaActual[i], numeroDeFila(i));
    }
    // Ojo: el cartel de "sin datos" y el contador miran el TOTAL, no cuántas filas
    // entraron en esta página. Si no, con 47 mascotas diría "10 mascotas".
    mostrarSinDatos(totalElementos === 0);
    mostrarContador(totalElementos, "mascota", "mascotas");
    dibujarPaginador();
}

function agregarFila(mascota, numeroDeLaFila) {
    const fila = document.createElement("tr");
    fila.className = "fila-clicable";
    fila.setAttribute("tabindex", "0");

    // textContent (en vez de innerHTML) evita que un dato con < o > rompa la página
    // La columna "#" es el número de fila, NO el id de la base: el id es el número
    // que le tocó al registro al crearse y saltea, que es justo lo que no se quiere ver.
    agregarCelda(fila, numeroDeLaFila, "col-opcional");
    agregarCelda(fila, iconoDeEspecie(mascota.especie) + " " + mascota.nombre);
    agregarCelda(fila, mascota.especie);
    agregarCelda(fila, mascota.raza, "col-opcional");
    agregarCelda(fila, fechaCorta(mascota.fechaNacimiento), "col-opcional");
    agregarCelda(fila, mascota.duenioNombre);

    const celdaAcciones = document.createElement("td");
    celdaAcciones.className = "text-end text-nowrap";

    // crearBotonAccion corta la propagación del click: tocar Editar o Eliminar NO
    // abre el panel de detalle, aunque el botón esté dentro de la fila.
    const botonEditar = crearBotonAccion("Editar", "btn-outline-primary me-1", function () {
        abrirModalEdicion(mascota);
    });
    const botonEliminar = crearBotonAccion("Eliminar", "btn-outline-danger", function () {
        eliminar(mascota);
    });
    celdaAcciones.appendChild(botonEditar);
    celdaAcciones.appendChild(botonEliminar);
    fila.appendChild(celdaAcciones);

    fila.addEventListener("click", function () {
        abrirPanelMascota(mascota);
    });
    document.getElementById("cuerpoTabla").appendChild(fila);
}


// ----------------------------------------------------------------------------
//  Panel lateral: la ficha de la mascota y sus turnos
// ----------------------------------------------------------------------------

async function abrirPanelMascota(mascota) {
    abrirPanel(iconoDeEspecie(mascota.especie) + " " + mascota.nombre, [
        ["Especie", mascota.especie],
        ["Raza", mascota.raza],
        ["Nacimiento", fechaCorta(mascota.fechaNacimiento)],
        ["Dueño", mascota.duenioNombre]
    ]);
    mostrarRelacionados("Turnos de " + mascota.nombre);
    try {
        const turnos = await pedir(URL_BASE + "/turnos/mascota/" + mascota.id);
        for (const turno of turnos) {
            agregarItemPanel(
                fechaCorta(turno.fecha) + " " + horaCorta(turno.hora),
                turno.motivo + " · " + turno.veterinarioNombre,
                turno.estado,
                colorDelEstado(turno.estado));
        }
        terminarRelacionados(turnos.length, "Esta mascota todavía no tiene turnos.");
    } catch (error) {
        mostrarAvisoPanel(error.message);
    }
}


// ----------------------------------------------------------------------------
//  Formulario
// ----------------------------------------------------------------------------

// Busca dueños por cédula, nombre, apellido o email y llena el desplegable con lo
// que coincide. Antes se traían TODOS al cargar la página: con muchos dueños, esa
// lista es imposible de usar.
async function buscarDuenios(texto) {
    const ayuda = document.getElementById("ayudaDuenioMascota");
    try {
        const respuesta = await pedir(urlParaCombo(URL_DUENIOS, texto));
        const duenios = contenidoDe(respuesta);
        llenarCombo("duenioId", duenios, "Elegí un dueño...", textoDeDuenio);

        if (duenios.length === 0) {
            ayuda.textContent = "Ningún dueño coincide con lo que escribiste.";
            return;
        }
        const total = totalDe(respuesta);
        if (total > duenios.length) {
            // El combo tiene un tope intencional: se avisa para que el usuario no crea
            // que ésas son todas las opciones y pueda afinar la búsqueda.
            ayuda.textContent = "Mostrando los primeros " + duenios.length + " de " + total +
                    ": afiná la búsqueda";
        } else {
            ayuda.textContent = total === 1
                    ? "1 dueño encontrado"
                    : total + " dueños encontrados";
        }

        // Con un solo resultado no tiene sentido obligar a elegirlo a mano
        if (duenios.length === 1) {
            document.getElementById("duenioId").value = duenios[0].id;
        }
    } catch (error) {
        ayuda.textContent = "";
        mostrarErrorDeFormulario(error.message);
    }
}

function textoDeDuenio(duenio) {
    return duenio.nombre + " " + duenio.apellido + " (cédula " + duenio.cedula + ")";
}

function abrirModalAlta() {
    cerrarPanel();  // si el panel quedó abierto, se cierra: dos capas oscuras se pisan
    idEnEdicion = null;
    document.getElementById("tituloModal").textContent = "Nueva mascota";
    document.getElementById("formulario").reset();
    document.getElementById("duenioId").disabled = false;
    document.getElementById("buscarDuenioMascota").disabled = false;
    ocultarError();
    // Con el texto vacío el backend devuelve todos: con pocos dueños no hay que escribir
    buscarDuenios("");
    modal.show();
}

async function abrirModalEdicion(mascota) {
    cerrarPanel();
    idEnEdicion = mascota.id;
    // El nombre en vez del id (que ya no se muestra en ninguna parte de la pantalla)
    document.getElementById("tituloModal").textContent = "Editar a " + mascota.nombre;
    document.getElementById("nombre").value = mascota.nombre;
    document.getElementById("especie").value = mascota.especie;
    document.getElementById("raza").value = mascota.raza === null ? "" : mascota.raza;
    document.getElementById("fechaNacimiento").value = mascota.fechaNacimiento === null ? "" : mascota.fechaNacimiento;
    ocultarError();
    modal.show();

    // El dueño no se puede cambiar, así que se muestra ESE dueño y nada más. Hay que
    // pedirlo porque el combo ahora sólo tiene lo que se haya buscado, y la mascota
    // únicamente trae el id y el nombre de pila de su dueño.
    document.getElementById("buscarDuenioMascota").value = "";
    document.getElementById("buscarDuenioMascota").disabled = true;
    document.getElementById("ayudaDuenioMascota").textContent = "El dueño de una mascota no se cambia.";
    try {
        // Es un recurso único, no un listado paginado: este endpoint no cambia.
        const duenio = await pedir(URL_DUENIOS + "/" + mascota.duenioId);
        llenarCombo("duenioId", [duenio], "Elegí un dueño...", textoDeDuenio);
        document.getElementById("duenioId").value = duenio.id;
    } catch (error) {
        // Si falla, al menos que el combo no quede vacío: se arma con lo que trae la mascota
        llenarCombo("duenioId", [{ id: mascota.duenioId, nombre: mascota.duenioNombre }],
                "Elegí un dueño...", function (item) { return item.nombre; });
        document.getElementById("duenioId").value = mascota.duenioId;
    }
    // Se deshabilita DESPUÉS de cargarlo: el valor igual se envía porque el DTO lo valida
    document.getElementById("duenioId").disabled = true;
}

function leerFormulario() {
    const raza = document.getElementById("raza").value.trim();
    const fechaNacimiento = document.getElementById("fechaNacimiento").value;
    const duenioId = document.getElementById("duenioId").value;
    return {
        nombre: document.getElementById("nombre").value.trim(),
        especie: document.getElementById("especie").value.trim(),
        // Los campos opcionales vacíos se mandan como null
        raza: raza === "" ? null : raza,
        fechaNacimiento: fechaNacimiento === "" ? null : fechaNacimiento,
        // Un campo numérico vacío se manda como null, no como texto vacío
        duenioId: duenioId === "" ? null : parseInt(duenioId, 10)
    };
}

async function guardar(evento) {
    evento.preventDefault();
    ocultarError();
    const datos = leerFormulario();
    // Alta y edición cambian solo en el método HTTP y en la URL
    const esEdicion = idEnEdicion !== null;
    const url = esEdicion ? URL_API + "/" + idEnEdicion : URL_API;
    const metodo = esEdicion ? "PUT" : "POST";

    try {
        await pedir(url, {
            method: metodo,
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(datos)
        });
        modal.hide();
        mostrarAviso(esEdicion ? "Mascota actualizada correctamente." : "Mascota creada correctamente.", "success");
        if (!esEdicion) {
            // Un alta cambia la lista entera (hay uno más y los cortes de página se
            // corren), así que se vuelve al principio. Una edición te deja donde estabas.
            volverAPrimeraPagina();
        }
        await recargarTabla();
    } catch (error) {
        // El error se muestra DENTRO del modal, sin cerrarlo, así no se pierde lo escrito.
        // Si el backend mandó errores de validación, se marca cada campo en rojo.
        mostrarErrorDeFormulario(error.message);
    }
}

async function eliminar(mascota) {
    const confirmado = window.confirm(
        "¿Eliminar a la mascota " + mascota.nombre + "?\n\n" +
        "Si tiene turnos registrados, la operación se rechaza.");
    if (!confirmado) {
        return;
    }
    try {
        await pedir(URL_API + "/" + mascota.id, { method: "DELETE" });
        mostrarAviso("Mascota eliminada correctamente.", "success");
        await recargarTabla();
    } catch (error) {
        mostrarAviso(error.message);
    }
}


// ----------------------------------------------------------------------------
//  Arranque: se ejecuta cuando el HTML terminó de cargar
// ----------------------------------------------------------------------------

function iniciar() {
    iniciarNucleo();
    modal = new bootstrap.Modal(document.getElementById("modalFormulario"));
    document.getElementById("botonNuevo").addEventListener("click", abrirModalAlta);
    document.getElementById("formulario").addEventListener("submit", guardar);
    // El buscador de la tabla le pide la lista filtrada al backend (?buscar=), con debounce
    crearBuscador(cargarTabla);
    // El del formulario es otro buscador distinto, con su propio temporizador
    crearBuscadorEn("buscarDuenioMascota", buscarDuenios);
    // Ordenar AHORA se lo pide al backend: ordenar en memoria sólo ordenaría la página
    // que estás viendo.
    habilitarOrdenPorColumna(recargarTabla);
    habilitarPaginador(recargarTabla);
    cargarTabla("");
}

document.addEventListener("DOMContentLoaded", iniciar);
