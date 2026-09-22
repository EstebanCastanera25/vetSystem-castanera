// ============================================================================
//  Turnos — lo propio de esta página.
//
//  Es la pantalla distinta de las cuatro: un turno no se edita ni se elimina
//  (es historial clínico), lo único que cambia es su ESTADO, y eso se hace con
//  un PATCH que manda los datos por la query string. Por eso esta página tiene
//  dos modales: uno para el alta y otro sólo para el cambio de estado.
//
//  Lo común (pedir a la API, avisos, orden, panel lateral) vive en app.js.
// ============================================================================

// URL_BASE la define app.js. Estas cuatro son de esta página.
const URL_API = URL_BASE + "/turnos";
const URL_DUENIOS = URL_BASE + "/duenios";
const URL_MASCOTAS = URL_BASE + "/mascotas";
const URL_VETERINARIOS = URL_BASE + "/veterinarios";

let idEnEdicion = null;   // el turno al que se le está cambiando el estado
let modal = null;
let modalEstado = null;

// Las filas de la página que se está viendo (NO la lista completa)
let listaActual = [];


// ----------------------------------------------------------------------------
//  Tabla
// ----------------------------------------------------------------------------

// estado es lo elegido en el desplegable ("" = todos)
async function cargarTabla(estado) {
    const filtro = (estado === undefined || estado === null) ? "" : estado;
    const numeroPedido = nuevoPedido();
    mostrarSpinnerTabla(true);
    try {
        // El filtro de esta pantalla se llama "estado" y no "buscar": por eso el nombre
        // del parámetro se le pasa a urlDeListado en vez de estar fijo adentro.
        const respuesta = await pedir(urlDeListado(URL_API, "estado", filtro));

        // Si mientras esperábamos salió otro pedido (otra página, otro orden), éste ya
        // quedó viejo: se descarta para no pisar al nuevo.
        if (!pedidoVigente(numeroPedido)) {
            return;
        }
        listaActual = leerPagina(respuesta);

        if (corregirPaginaVacia()) {
            await cargarTabla(filtro);
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
        if (pedidoVigente(numeroPedido)) {
            mostrarSpinnerTabla(false);
        }
    }
}

// El filtro que está elegido AHORA en la pantalla. Se lee del DOM y no de una variable
// para que no puedan quedar desincronizados: lo que se ve es la verdad.
function filtroDeLaPagina() {
    return document.getElementById("filtroEstado").value;
}

// Vuelve a pedir la página actual con el filtro actual. La usan el orden, el paginador,
// el selector de tamaño, guardar() y guardarEstado().
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
    // El cartel de "sin datos" y el contador miran el TOTAL, no cuántas filas entraron
    // en esta página.
    mostrarSinDatos(totalElementos === 0);
    mostrarContador(totalElementos, "turno", "turnos");
    dibujarPaginador();
}

function agregarFila(turno, numeroDeLaFila) {
    const fila = document.createElement("tr");
    fila.className = "fila-clicable";
    fila.setAttribute("tabindex", "0");

    // La columna "#" es el número de fila, NO el id de la base: el id es el número
    // que le tocó al registro al crearse y saltea, que es justo lo que no se quiere ver.
    agregarCelda(fila, numeroDeLaFila, "col-opcional");
    agregarCelda(fila, fechaCorta(turno.fecha));
    // La API devuelve la hora como "10:30:00"; en la tabla alcanza "10:30"
    agregarCelda(fila, horaCorta(turno.hora));
    agregarCelda(fila, turno.mascotaNombre);
    agregarCelda(fila, turno.veterinarioNombre);
    agregarCelda(fila, turno.motivo, "col-opcional");
    // El estado se muestra como una etiqueta de color
    agregarCeldaBadge(fila, turno.estado, colorDelEstado(turno.estado));
    agregarCelda(fila, turno.observaciones, "col-opcional");

    const celdaAcciones = document.createElement("td");
    celdaAcciones.className = "text-end text-nowrap";

    // crearBotonAccion corta la propagación del click: tocar el botón NO abre el panel
    const botonEstado = crearBotonAccion("Cambiar estado", "btn-outline-primary", function () {
        abrirModalEstado(turno);
    });
    celdaAcciones.appendChild(botonEstado);
    fila.appendChild(celdaAcciones);

    fila.addEventListener("click", function () {
        abrirPanelTurno(turno);
    });

    document.getElementById("cuerpoTabla").appendChild(fila);
}


// ----------------------------------------------------------------------------
//  Panel lateral: la ficha del turno y el historial de esa mascota
// ----------------------------------------------------------------------------

async function abrirPanelTurno(turno) {
    abrirPanel("Turno del " + fechaCorta(turno.fecha), [
        ["Fecha", fechaCorta(turno.fecha)],
        ["Hora", horaCorta(turno.hora)],
        ["Mascota", turno.mascotaNombre],
        ["Veterinario", turno.veterinarioNombre],
        ["Motivo", turno.motivo],
        ["Estado", turno.estado],
        ["Observaciones", turno.observaciones]
    ]);

    mostrarRelacionados("Historial de " + turno.mascotaNombre);
    try {
        const turnos = await pedir(URL_API + "/mascota/" + turno.mascotaId);
        let mostrados = 0;
        for (const otro of turnos) {
            // El turno que se está mirando no se repite en su propio historial
            if (otro.id === turno.id) {
                continue;
            }
            agregarItemPanel(
                fechaCorta(otro.fecha) + " " + horaCorta(otro.hora),
                otro.motivo + " · " + otro.veterinarioNombre,
                otro.estado,
                colorDelEstado(otro.estado));
            mostrados = mostrados + 1;
        }
        terminarRelacionados(mostrados, "Es el único turno de esta mascota.");
    } catch (error) {
        mostrarAvisoPanel(error.message);
    }
}


// ----------------------------------------------------------------------------
//  Elegir la mascota: primero se busca al dueño, después se eligen sus mascotas
// ----------------------------------------------------------------------------

// Busca dueños por cédula, nombre, apellido o email. Si NINGUNO coincide, prueba
// buscar mascotas con el mismo texto: así escribir "Firulais" también sirve, sin
// tener que saber de quién es.
async function buscarDuenios(texto) {
    const ayuda = document.getElementById("ayudaDuenio");
    try {
        // Un combo NO se pagina: se pide un tamaño grande para que estén todas las
        // opciones que coinciden.
        const respuesta = await pedir(urlParaCombo(URL_DUENIOS, texto));
        const duenios = contenidoDe(respuesta);

        llenarCombo("duenioDelTurno", duenios, "Elegí un dueño...", function (duenio) {
            return duenio.nombre + " " + duenio.apellido + " (cédula " + duenio.cedula + ")";
        });

        if (duenios.length === 0) {
            // Camino directo: el texto no es de ningún dueño, puede ser una mascota
            await buscarMascotasSueltas(texto);
            return;
        }

        ayuda.textContent = duenios.length === 1
                ? "1 dueño encontrado"
                : duenios.length + " dueños encontrados";

        // Con un solo resultado no tiene sentido obligar a elegirlo a mano
        if (duenios.length === 1) {
            document.getElementById("duenioDelTurno").value = duenios[0].id;
            await cargarMascotasDelDuenio(duenios[0].id, duenios[0].nombre);
        } else {
            vaciarComboMascotas("Elegí primero un dueño...", "");
        }
    } catch (error) {
        ayuda.textContent = "";
        mostrarErrorDeFormulario(error.message);
    }
}

// Las mascotas que coinciden con el texto, sin pasar por el dueño. El buscador del
// backend mira el nombre, la especie, la raza y también el nombre del dueño.
async function buscarMascotasSueltas(texto) {
    const ayudaDuenio = document.getElementById("ayudaDuenio");
    const respuesta = await pedir(urlParaCombo(URL_MASCOTAS, texto));
    const mascotas = contenidoDe(respuesta);

    llenarCombo("mascotaId", mascotas, "Elegí una mascota...", textoDeMascota);

    if (mascotas.length === 0) {
        ayudaDuenio.textContent = "No hay ningún dueño ni ninguna mascota que coincida.";
        document.getElementById("ayudaMascota").textContent = "";
    } else {
        ayudaDuenio.textContent = "Ningún dueño coincide.";
        document.getElementById("ayudaMascota").textContent =
                "Estas son las mascotas que coinciden con lo que escribiste.";
        if (mascotas.length === 1) {
            document.getElementById("mascotaId").value = mascotas[0].id;
        }
    }
}

// Las mascotas de UN dueño: el endpoint anidado que ya existe en la API
async function cargarMascotasDelDuenio(duenioId, nombreDuenio) {
    const ayuda = document.getElementById("ayudaMascota");
    try {
        // Este endpoint NO se pagina (un dueño tiene pocas mascotas): devuelve un
        // array pelado, así que se usa tal cual.
        const mascotas = await pedir(URL_DUENIOS + "/" + duenioId + "/mascotas");
        llenarCombo("mascotaId", mascotas, "Elegí una mascota...", textoDeMascota);

        if (mascotas.length === 0) {
            // Un combo vacío sin explicación deja al usuario sin saber qué pasó
            ayuda.textContent = nombreDuenio + " no tiene mascotas registradas.";
            return;
        }
        if (mascotas.length === 1) {
            document.getElementById("mascotaId").value = mascotas[0].id;
            ayuda.textContent = "Tiene una sola mascota, ya quedó elegida.";
            return;
        }
        ayuda.textContent = "Tiene " + mascotas.length + " mascotas.";
    } catch (error) {
        ayuda.textContent = "";
        mostrarErrorDeFormulario(error.message);
    }
}

function textoDeMascota(mascota) {
    return iconoDeEspecie(mascota.especie) + " " + mascota.nombre + " (" + mascota.especie + ")";
}

function vaciarComboMascotas(textoPlaceholder, textoAyuda) {
    llenarCombo("mascotaId", [], textoPlaceholder, textoDeMascota);
    document.getElementById("ayudaMascota").textContent = textoAyuda;
}

// ----------------------------------------------------------------------------
//  Elegir el veterinario
// ----------------------------------------------------------------------------

async function buscarVeterinarios(texto) {
    const ayuda = document.getElementById("ayudaVeterinario");
    try {
        const respuesta = await pedir(urlParaCombo(URL_VETERINARIOS, texto));
        const veterinarios = contenidoDe(respuesta);

        llenarCombo("veterinarioId", veterinarios, "Elegí un veterinario...", function (veterinario) {
            return veterinario.nombre + " " + veterinario.apellido + " — " + veterinario.especialidad;
        });

        if (veterinarios.length === 0) {
            ayuda.textContent = "Ningún veterinario coincide.";
            return;
        }
        ayuda.textContent = "";
        if (veterinarios.length === 1) {
            document.getElementById("veterinarioId").value = veterinarios[0].id;
        }
    } catch (error) {
        ayuda.textContent = "";
        mostrarErrorDeFormulario(error.message);
    }
}


// ----------------------------------------------------------------------------
//  Alta de un turno
// ----------------------------------------------------------------------------

function abrirModalAlta() {
    cerrarPanel();  // si el panel quedó abierto, se cierra: dos capas oscuras se pisan
    // reset() vacía los inputs, PERO no borra las <option> que quedaron del turno
    // anterior: por eso abajo se vuelven a pedir las listas.
    document.getElementById("formulario").reset();
    // La fecha mínima es hoy, porque el backend rechaza los turnos en el pasado.
    // fechaDeHoy() (app.js) se arma a mano y NO con toISOString(), que usa UTC:
    // de noche, en Argentina, devolvería la fecha de mañana.
    document.getElementById("fecha").min = fechaDeHoy();
    ocultarError();

    // Con el texto vacío el backend devuelve todo, así que con pocos registros no hace
    // falta escribir nada. Se piden ACÁ y no al cargar la página: si se dio de alta una
    // mascota hace un minuto, aparece igual sin tener que recargar.
    vaciarComboMascotas("Elegí primero un dueño...", "");
    buscarDuenios("");
    buscarVeterinarios("");

    modal.show();
}

function leerFormulario() {
    const mascotaId = document.getElementById("mascotaId").value;
    const veterinarioId = document.getElementById("veterinarioId").value;
    const hora = document.getElementById("hora").value;
    const motivo = document.getElementById("motivo").value.trim();

    return {
        fecha: document.getElementById("fecha").value === "" ? null : document.getElementById("fecha").value,
        // El input type="time" devuelve "10:30" y el backend espera "10:30:00"
        hora: hora === "" ? null : (hora.length === 5 ? hora + ":00" : hora),
        motivo: motivo === "" ? null : motivo,
        mascotaId: mascotaId === "" ? null : parseInt(mascotaId, 10),
        veterinarioId: veterinarioId === "" ? null : parseInt(veterinarioId, 10)
    };
}

async function guardar(evento) {
    evento.preventDefault();
    ocultarError();
    try {
        await pedir(URL_API, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(leerFormulario())
        });
        modal.hide();
        mostrarAviso("Turno creado correctamente.", "success");
        // Un alta cambia la lista entera: se vuelve al principio, igual que al filtrar
        volverAPrimeraPagina();
        await recargarTabla();
    } catch (error) {
        // El error se muestra DENTRO del modal, sin cerrarlo, así no se pierde lo cargado.
        // Si el backend mandó errores de validación, se marca cada campo en rojo; si fue
        // un 409 por superposición de horarios, se muestra el mensaje completo.
        mostrarErrorDeFormulario(error.message);
    }
}


// ----------------------------------------------------------------------------
//  Cambio de estado (PATCH): es una actualización parcial, sólo del estado
// ----------------------------------------------------------------------------

function abrirModalEstado(turno) {
    cerrarPanel();
    idEnEdicion = turno.id;
    // La mascota en vez del id (que ya no se muestra en ninguna parte de la pantalla)
    document.getElementById("tituloEstado").textContent =
            "Cambiar el estado del turno de " + turno.mascotaNombre;
    document.getElementById("estado").value = turno.estado;
    document.getElementById("observaciones").value = turno.observaciones === null ? "" : turno.observaciones;
    ocultarErrorEstado();
    modalEstado.show();
}

async function guardarEstado(evento) {
    evento.preventDefault();
    ocultarErrorEstado();

    const estado = document.getElementById("estado").value;
    const observaciones = document.getElementById("observaciones").value.trim();

    // Este endpoint recibe los datos por la query string, no en el cuerpo.
    // encodeURIComponent escapa los caracteres que tienen otro significado en
    // una URL (tildes, espacios, el &...).
    let url = URL_API + "/" + idEnEdicion + "/estado?estado=" + encodeURIComponent(estado);
    if (observaciones !== "") {
        // Sólo se agrega si hay texto: mandarlo vacío borraría las observaciones guardadas
        url = url + "&observaciones=" + encodeURIComponent(observaciones);
    }

    try {
        await pedir(url, { method: "PATCH" });
        modalEstado.hide();
        mostrarAviso("Estado actualizado correctamente.", "success");
        // Cambiar el estado de UN turno no mueve la lista: te quedás donde estabas
        await recargarTabla();
    } catch (error) {
        mostrarErrorEstado(error.message);
    }
}

// Este modal tiene su propio cartel de error: las funciones del núcleo escriben
// en el del formulario de alta (#errorFormulario), que es otro.
function mostrarErrorEstado(mensaje) {
    const cartel = document.getElementById("errorEstado");
    cartel.textContent = mensaje;
    cartel.classList.remove("d-none");
}

function ocultarErrorEstado() {
    document.getElementById("errorEstado").classList.add("d-none");
}


// ----------------------------------------------------------------------------
//  Arranque: se ejecuta cuando el HTML terminó de cargar
// ----------------------------------------------------------------------------

function iniciar() {
    iniciarNucleo();
    modal = new bootstrap.Modal(document.getElementById("modalFormulario"));
    modalEstado = new bootstrap.Modal(document.getElementById("modalEstado"));
    document.getElementById("botonNuevo").addEventListener("click", abrirModalAlta);
    document.getElementById("formulario").addEventListener("submit", guardar);
    document.getElementById("formularioEstado").addEventListener("submit", guardarEstado);

    // El filtro por estado le pide la lista filtrada al backend (?estado=). Esta pantalla
    // no usa crearBuscador (su filtro es un desplegable), así que el "volver a la primera
    // página" va a mano.
    document.getElementById("filtroEstado").addEventListener("change", function () {
        volverAPrimeraPagina();
        recargarTabla();
    });
    // Ordenar AHORA se lo pide al backend: ordenar en memoria sólo ordenaría la página
    // que estás viendo.
    habilitarOrdenPorColumna(recargarTabla);
    habilitarPaginador(recargarTabla);

    // Los dos buscadores del modal. Cada uno tiene su propio temporizador (está
    // declarado dentro de crearBuscadorEn), así que escribir en uno no le corta el
    // debounce al otro.
    crearBuscadorEn("buscarDuenio", buscarDuenios);
    crearBuscadorEn("buscarVeterinario", buscarVeterinarios);

    // Al elegir un dueño se cargan SUS mascotas
    document.getElementById("duenioDelTurno").addEventListener("change", function () {
        const combo = document.getElementById("duenioDelTurno");
        if (combo.value === "") {
            vaciarComboMascotas("Elegí primero un dueño...", "");
            return;
        }
        // El texto de la opción es "Nombre Apellido (cédula X)": alcanza la primera
        // palabra para el mensaje de "no tiene mascotas".
        const nombre = combo.options[combo.selectedIndex].textContent.split(" ")[0];
        cargarMascotasDelDuenio(combo.value, nombre);
    });

    cargarTabla("");
}

document.addEventListener("DOMContentLoaded", iniciar);
