// ============================================================================
//  Dueños — lo propio de esta página.
//
//  Todo lo que es igual en las cuatro páginas (pedir a la API, avisos, buscador,
//  orden, panel lateral) vive en app.js, que se carga antes que este archivo.
//  Acá queda solamente lo que cambia de una entidad a otra: las columnas de la
//  tabla, los campos del formulario y qué se muestra en el panel de detalle.
// ============================================================================

// URL_BASE la define app.js. URL_API la define CADA página: si app.js también la
// declarara, serían dos const con el mismo nombre y el navegador no ejecutaría nada.
const URL_API = URL_BASE + "/duenios";

// Guarda el id del dueño que se está editando. Si vale null, el formulario es un alta.
let idEnEdicion = null;
let modal = null;

// Las filas de la página que se está viendo (NO la lista completa: el backend manda
// de a una página por vez).
let listaActual = [];


// ----------------------------------------------------------------------------
//  Tabla
// ----------------------------------------------------------------------------

// texto es lo que haya escrito el usuario en el buscador ("" = traer todos)
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
    // entraron en esta página. Si no, con 47 dueños diría "10 dueños".
    mostrarSinDatos(totalElementos === 0);
    mostrarContador(totalElementos, "dueño", "dueños");
    dibujarPaginador();
}

function agregarFila(duenio, numeroDeLaFila) {
    const fila = document.createElement("tr");
    fila.className = "fila-clicable";
    fila.setAttribute("tabindex", "0");

    // textContent (en vez de innerHTML) evita que un dato con < o > rompa la página
    // La columna "#" es el número de fila, NO el id de la base: el id es el número
    // que le tocó al registro al crearse y saltea, que es justo lo que no se quiere ver.
    agregarCelda(fila, numeroDeLaFila, "col-opcional");
    agregarCelda(fila, duenio.nombre);
    agregarCelda(fila, duenio.apellido);
    agregarCelda(fila, duenio.cedula);
    agregarCelda(fila, duenio.telefono, "col-opcional");
    agregarCelda(fila, duenio.email, "col-opcional");

    const celdaAcciones = document.createElement("td");
    celdaAcciones.className = "text-end text-nowrap";

    // crearBotonAccion corta la propagación del click: tocar Editar o Eliminar NO
    // abre el panel de detalle, aunque el botón esté dentro de la fila.
    const botonEditar = crearBotonAccion("Editar", "btn-outline-primary me-1", function () {
        abrirModalEdicion(duenio);
    });
    const botonEliminar = crearBotonAccion("Eliminar", "btn-outline-danger", function () {
        eliminar(duenio);
    });

    celdaAcciones.appendChild(botonEditar);
    celdaAcciones.appendChild(botonEliminar);
    fila.appendChild(celdaAcciones);

    // La fila entera abre el panel lateral
    fila.addEventListener("click", function () {
        abrirPanelDuenio(duenio);
    });

    document.getElementById("cuerpoTabla").appendChild(fila);
}


// ----------------------------------------------------------------------------
//  Panel lateral: la ficha del dueño y sus mascotas
// ----------------------------------------------------------------------------

async function abrirPanelDuenio(duenio) {
    abrirPanel(duenio.nombre + " " + duenio.apellido, [
        ["Cédula", duenio.cedula],
        ["Teléfono", duenio.telefono],
        ["Email", duenio.email]
    ]);

    mostrarRelacionados("Mascotas de " + duenio.nombre);
    try {
        // Endpoint anidado: las mascotas SON un dato del dueño, por eso cuelgan de él.
        // Este listado NO se pagina (un dueño tiene pocas mascotas): devuelve un array
        // pelado, así que se recorre directo.
        const mascotas = await pedir(URL_API + "/" + duenio.id + "/mascotas");
        for (const mascota of mascotas) {
            let detalle = mascota.especie;
            if (mascota.raza) {
                detalle = detalle + " · " + mascota.raza;
            }
            agregarItemPanel(
                iconoDeEspecie(mascota.especie) + " " + mascota.nombre,
                detalle,
                mascota.fechaNacimiento ? fechaCorta(mascota.fechaNacimiento) : "",
                "text-bg-light");
        }
        terminarRelacionados(mascotas.length, "Este dueño todavía no tiene mascotas registradas.");
    } catch (error) {
        mostrarAvisoPanel(error.message);
    }
}


// ----------------------------------------------------------------------------
//  Formulario
// ----------------------------------------------------------------------------

function abrirModalAlta() {
    cerrarPanel();  // si el panel quedó abierto, se cierra: dos capas oscuras se pisan
    idEnEdicion = null;
    document.getElementById("tituloModal").textContent = "Nuevo dueño";
    document.getElementById("formulario").reset();
    document.getElementById("cedula").readOnly = false;
    ocultarError();
    modal.show();
}

function abrirModalEdicion(duenio) {
    cerrarPanel();
    idEnEdicion = duenio.id;
    // El nombre en vez del id: el id ya no se muestra en ninguna parte de la pantalla,
    // y ver a quién estás editando es más útil que un número
    document.getElementById("tituloModal").textContent =
            "Editar a " + duenio.nombre + " " + duenio.apellido;
    document.getElementById("nombre").value = duenio.nombre;
    document.getElementById("apellido").value = duenio.apellido;
    document.getElementById("cedula").value = duenio.cedula;
    document.getElementById("telefono").value = duenio.telefono;
    document.getElementById("email").value = duenio.email;
    // La cédula no se puede cambiar, pero igual se envía porque el DTO la valida
    document.getElementById("cedula").readOnly = true;
    ocultarError();
    modal.show();
}

function leerFormulario() {
    const telefono = document.getElementById("telefono").value;
    return {
        nombre: document.getElementById("nombre").value.trim(),
        apellido: document.getElementById("apellido").value.trim(),
        cedula: document.getElementById("cedula").value.trim(),
        // Un campo numérico vacío se manda como null, no como texto vacío
        telefono: telefono === "" ? null : parseInt(telefono, 10),
        email: document.getElementById("email").value.trim()
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
        mostrarAviso(esEdicion ? "Dueño actualizado correctamente." : "Dueño creado correctamente.", "success");
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

async function eliminar(duenio) {
    const confirmado = window.confirm(
        "¿Eliminar al dueño " + duenio.nombre + " " + duenio.apellido + "?\n\n" +
        "Se borran también sus mascotas. Si alguna tiene turnos registrados, " +
        "la operación se rechaza.");
    if (!confirmado) {
        return;
    }
    try {
        await pedir(URL_API + "/" + duenio.id, { method: "DELETE" });
        mostrarAviso("Dueño eliminado correctamente.", "success");
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

    // El buscador le pide la lista filtrada al backend (?buscar=), con debounce
    crearBuscador(cargarTabla);
    // Ordenar AHORA se lo pide al backend: ordenar en memoria sólo ordenaría la página
    // que estás viendo.
    habilitarOrdenPorColumna(recargarTabla);
    habilitarPaginador(recargarTabla);

    cargarTabla("");
}

document.addEventListener("DOMContentLoaded", iniciar);
