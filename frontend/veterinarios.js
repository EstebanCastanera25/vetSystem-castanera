// ============================================================================
//  Veterinarios — lo propio de esta página.
//
//  Lo compartido vive en app.js. Acá queda lo que cambia de esta entidad: las
//  columnas, los campos del formulario y el contenido del panel de detalle.
// ============================================================================

// URL_BASE la define app.js. URL_API la define CADA página: si app.js también la
// declarara, serían dos const con el mismo nombre y el navegador no ejecutaría nada.
const URL_API = URL_BASE + "/veterinarios";

let idEnEdicion = null;
let modal = null;
let listaActual = [];
let veterinarioDelPanel = null;


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
    // entraron en esta página. Si no, con 47 veterinarios diría "10 veterinarios".
    mostrarSinDatos(totalElementos === 0);
    mostrarContador(totalElementos, "veterinario", "veterinarios");
    dibujarPaginador();
}

function agregarFila(veterinario, numeroDeLaFila) {
    const fila = document.createElement("tr");
    fila.className = "fila-clicable";
    fila.setAttribute("tabindex", "0");

    // textContent evita que un dato con < o > se interprete como HTML.
    // La columna "#" es el número de fila, NO el id de la base: el id es el número
    // que le tocó al registro al crearse y saltea, que es justo lo que no se quiere ver.
    agregarCelda(fila, numeroDeLaFila, "col-opcional");
    agregarCelda(fila, veterinario.nombre);
    agregarCelda(fila, veterinario.apellido);
    agregarCelda(fila, veterinario.matricula);
    agregarCelda(fila, veterinario.especialidad, "col-opcional");
    agregarCelda(fila, veterinario.email, "col-opcional");

    const celdaAcciones = document.createElement("td");
    celdaAcciones.className = "text-end text-nowrap";

    // crearBotonAccion corta la propagación del click: tocar Editar o Eliminar NO
    // abre el panel de detalle, aunque el botón esté dentro de la fila.
    const botonEditar = crearBotonAccion("Editar", "btn-outline-primary me-1", function () {
        abrirModalEdicion(veterinario);
    });
    const botonEliminar = crearBotonAccion("Eliminar", "btn-outline-danger", function () {
        eliminar(veterinario);
    });
    celdaAcciones.appendChild(botonEditar);
    celdaAcciones.appendChild(botonEliminar);
    fila.appendChild(celdaAcciones);

    fila.addEventListener("click", function () {
        abrirPanelVeterinario(veterinario);
    });
    document.getElementById("cuerpoTabla").appendChild(fila);
}


// ----------------------------------------------------------------------------
//  Panel lateral: la ficha del veterinario y su agenda
// ----------------------------------------------------------------------------

function abrirPanelVeterinario(veterinario) {
    veterinarioDelPanel = veterinario;
    abrirPanel(veterinario.nombre + " " + veterinario.apellido, [
        ["Matrícula", veterinario.matricula],
        ["Especialidad", veterinario.especialidad],
        ["Email", veterinario.email]
    ]);
    document.getElementById("fechaAgenda").value = fechaDeHoy();
    cargarAgenda();
}

async function cargarAgenda() {
    if (veterinarioDelPanel === null) {
        return;
    }
    const fecha = document.getElementById("fechaAgenda").value;
    mostrarRelacionados("Turnos del " + fechaCorta(fecha));
    try {
        // La agenda es una lista acotada que no se paginó: este endpoint no cambia.
        const turnos = await pedir(URL_BASE + "/turnos/agenda?veterinarioId=" +
            veterinarioDelPanel.id + "&fecha=" + fecha);
        for (const turno of turnos) {
            agregarItemPanel(horaCorta(turno.hora) + " · " + turno.mascotaNombre,
                turno.motivo, turno.estado, colorDelEstado(turno.estado));
        }
        terminarRelacionados(turnos.length, "No tiene turnos para esa fecha.");
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
    document.getElementById("tituloModal").textContent = "Nuevo veterinario";
    document.getElementById("formulario").reset();
    document.getElementById("matricula").readOnly = false;
    ocultarError();
    modal.show();
}

function abrirModalEdicion(veterinario) {
    cerrarPanel();  // evita apilar el fondo oscuro del panel con el del modal
    idEnEdicion = veterinario.id;
    // El nombre en vez del id (que ya no se muestra en ninguna parte de la pantalla)
    document.getElementById("tituloModal").textContent =
            "Editar a " + veterinario.nombre + " " + veterinario.apellido;
    document.getElementById("nombre").value = veterinario.nombre;
    document.getElementById("apellido").value = veterinario.apellido;
    document.getElementById("matricula").value = veterinario.matricula;
    document.getElementById("especialidad").value = veterinario.especialidad;
    document.getElementById("email").value = veterinario.email === null ? "" : veterinario.email;
    // La matrícula no se puede cambiar, pero igual se envía porque el DTO la valida.
    document.getElementById("matricula").readOnly = true;
    ocultarError();
    modal.show();
}

function leerFormulario() {
    const email = document.getElementById("email").value.trim();
    return {
        nombre: document.getElementById("nombre").value.trim(),
        apellido: document.getElementById("apellido").value.trim(),
        matricula: document.getElementById("matricula").value.trim(),
        especialidad: document.getElementById("especialidad").value.trim(),
        // Un campo de texto opcional vacío se manda como null.
        email: email === "" ? null : email
    };
}

async function guardar(evento) {
    evento.preventDefault();
    ocultarError();
    const datos = leerFormulario();
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
        mostrarAviso(esEdicion ? "Veterinario actualizado correctamente." :
            "Veterinario creado correctamente.", "success");
        if (!esEdicion) {
            // Un alta cambia la lista entera (hay uno más y los cortes de página se
            // corren), así que se vuelve al principio. Una edición te deja donde estabas.
            volverAPrimeraPagina();
        }
        await recargarTabla();
    } catch (error) {
        // El modal queda abierto y, si son validaciones, se marca cada campo en rojo.
        mostrarErrorDeFormulario(error.message);
    }
}

async function eliminar(veterinario) {
    const confirmado = window.confirm(
        "¿Eliminar al veterinario " + veterinario.nombre + " " + veterinario.apellido + "?\n\n" +
        "Se borran también todos sus turnos.");
    if (!confirmado) {
        return;
    }
    try {
        await pedir(URL_API + "/" + veterinario.id, { method: "DELETE" });
        mostrarAviso("Veterinario eliminado correctamente.", "success");
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
    crearBuscador(cargarTabla);
    // Ordenar AHORA se lo pide al backend: ordenar en memoria sólo ordenaría la página
    // que estás viendo.
    habilitarOrdenPorColumna(recargarTabla);
    habilitarPaginador(recargarTabla);
    document.getElementById("fechaAgenda").addEventListener("change", function () {
        cargarAgenda();
    });
    cargarTabla("");
}

document.addEventListener("DOMContentLoaded", iniciar);
