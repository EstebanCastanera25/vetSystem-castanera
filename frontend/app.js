// ============================================================================
//  app.js — núcleo compartido por las cuatro páginas del frontend
//
//  Antes cada HTML repetía el mismo fetch, los mismos avisos y la misma forma de
//  armar la tabla: alrededor de 700 líneas copiadas entre páginas. Acá vive UNA
//  sola vez todo lo que no depende de la entidad (pedir a la API, avisos, orden,
//  buscador, panel de detalle) y cada página se queda solamente con lo suyo:
//  qué columnas tiene su tabla y qué campos tiene su formulario.
//
//  Se carga como script clásico (<script src="app.js">), NO como módulo. Un
//  módulo obligaría al servidor a mandar el MIME correcto para .js, y eso depende
//  del registro de Windows: si en otra máquina .js estuviera registrado como
//  text/plain, la página entera dejaría de funcionar. Un script clásico no.
//
//  Consecuencia de ser un script clásico: TODO lo que se declara acá es global.
//  Por eso este archivo sólo declara las tres constantes de abajo, y cada página
//  declara su propia URL_API. Si las dos declararan la misma const, el navegador
//  tira un SyntaxError y no se ejecuta NADA de la página.
// ============================================================================

const URL_BASE = "http://localhost:8080/api";
const MS_AVISO = 4000;     // cuánto dura un aviso antes de cerrarse solo
const MS_DEBOUNCE = 300;   // cuánto se espera después de la última tecla para buscar

const TAMANIO_PAGINA_INICIAL = 10;  // cuántas filas trae la tabla al abrir la pantalla
const MAX_BOTONES_PAGINA = 5;       // cuántos números de página se dibujan como máximo
const TAMANIO_COMBO = 200;          // cuántas opciones se le piden a un <select> del formulario

// Estado del núcleo (lo usan las funciones de abajo, no las páginas)
let temporizadorAviso = null;
let campoOrden = null;
let ordenAscendente = true;
let panelDetalle = null;

// Estado de la paginación. Vive acá, junto a campoOrden, porque es lo mismo: datos de
// "cómo se está mirando la tabla". Cada HTML es un documento aparte con su propio
// contexto de JavaScript, así que dos páginas nunca comparten estas variables.
let paginaActual = 0;               // el backend cuenta desde 0; el "+1" existe sólo al dibujar
let tamanioPagina = TAMANIO_PAGINA_INICIAL;
let totalPaginas = 0;
let totalElementos = 0;
let contadorPedidos = 0;            // número del último pedido de listado que se lanzó


// ============================================================================
//  1. Cliente HTTP — el único lugar del frontend que llama a la API
// ============================================================================

// Devuelve el JSON ya parseado. Si la respuesta es un error, lanza una excepción
// con el mensaje que mandó el backend, así cada página decide dónde mostrarlo.
async function pedir(url, opciones) {
    let respuesta;
    try {
        respuesta = await fetch(url, opciones);
    } catch (error) {
        // Acá cae la falta de conexión y el bloqueo por CORS
        throw new Error("No se pudo conectar con la API. ¿Está corriendo en el puerto 8080?");
    }

    // El 204 (No Content) del DELETE no trae cuerpo: no se puede leer como JSON
    if (respuesta.status === 204) {
        return null;
    }

    let cuerpo = null;
    try {
        cuerpo = await respuesta.json();
    } catch (error) {
        cuerpo = null;
    }

    if (!respuesta.ok) {
        let mensaje = "El servidor respondió con el estado " + respuesta.status;
        // El backend responde siempre un ErrorResponse, y su campo se llama "mensaje"
        if (cuerpo !== null && cuerpo.mensaje) {
            mensaje = cuerpo.mensaje;
        }
        const error = new Error(mensaje);
        error.estado = respuesta.status;  // por si la página quiere distinguir un 404 de un 500
        throw error;
    }
    return cuerpo;
}


// ============================================================================
//  2. Tabla
// ============================================================================

// Crea un <td> con textContent (NO innerHTML): si un dato trae < o >, se ve como
// texto en vez de interpretarse como HTML.
function agregarCelda(fila, valor, clase) {
    const celda = document.createElement("td");
    if (valor === null || valor === undefined) {
        celda.textContent = "";
    } else {
        celda.textContent = valor;
    }
    if (clase) {
        celda.className = clase;
    }
    fila.appendChild(celda);
    return celda;
}

// Un <td> con una etiqueta de color adentro (estado de un turno, especie, etc.)
function agregarCeldaBadge(fila, texto, claseBadge, claseCelda) {
    const celda = document.createElement("td");
    if (claseCelda) {
        celda.className = claseCelda;
    }
    const etiqueta = document.createElement("span");
    etiqueta.className = "badge " + claseBadge;
    etiqueta.textContent = texto;
    celda.appendChild(etiqueta);
    fila.appendChild(celda);
    return celda;
}

// Los botones de la columna Acciones SIEMPRE se crean con esta función.
//
// El stopPropagation es la razón de que exista: la fila entera tiene un click que
// abre el panel de detalle, y un click en un botón también es un click en la fila.
// Sin cortar la propagación, tocar "Eliminar" y cancelar el confirm igual abriría
// el panel. Al estar acá, el problema está resuelto en un solo lugar.
function crearBotonAccion(texto, clase, alHacerClick) {
    const boton = document.createElement("button");
    boton.type = "button";
    boton.className = "btn btn-sm " + clase;
    boton.textContent = texto;
    boton.addEventListener("click", function (evento) {
        evento.stopPropagation();
        alHacerClick();
    });
    return boton;
}

function vaciarTabla() {
    // innerHTML con una cadena vacía es la forma corta de borrar todas las filas.
    // Es seguro porque no hay ningún dato adentro: es una constante.
    document.getElementById("cuerpoTabla").innerHTML = "";
}

function mostrarSinDatos(vacia) {
    const cartel = document.getElementById("sinDatos");
    if (cartel === null) {
        return;
    }
    if (vacia) {
        cartel.classList.remove("d-none");
    } else {
        cartel.classList.add("d-none");
    }
}

// "12 dueños" / "1 dueño" / "Sin resultados"
function mostrarContador(cantidad, singular, plural) {
    const cartel = document.getElementById("contadorResultados");
    if (cartel === null) {
        return;
    }
    if (cantidad === 0) {
        cartel.textContent = "Sin resultados";
    } else if (cantidad === 1) {
        cartel.textContent = "1 " + singular;
    } else {
        cartel.textContent = cantidad + " " + plural;
    }
}

function mostrarSpinnerTabla(activo) {
    const spinner = document.getElementById("spinnerTabla");
    if (spinner === null) {
        return;
    }
    if (activo) {
        spinner.classList.remove("d-none");
    } else {
        spinner.classList.add("d-none");
    }
}


// ============================================================================
//  3. Mensajes en pantalla
// ============================================================================

// Aviso de la página (arriba de la tabla). Se cierra solo a los MS_AVISO.
function mostrarAviso(mensaje, tipo) {
    const cartel = document.getElementById("aviso");
    cartel.textContent = mensaje;
    cartel.className = "alert alert-" + (tipo === "success" ? "success" : "danger");

    // Si ya había un aviso esperando para cerrarse, se cancela su temporizador:
    // si no, el aviso nuevo heredaría lo que le quedaba al anterior.
    if (temporizadorAviso !== null) {
        clearTimeout(temporizadorAviso);
    }
    temporizadorAviso = setTimeout(ocultarAviso, MS_AVISO);
}

function ocultarAviso() {
    // Se reasigna la lista ENTERA de clases, así vuelve a estar d-none. Antes se
    // hacía className = "alert alert-danger", que sacaba d-none y nunca la reponía:
    // el aviso quedaba pegado en pantalla para siempre.
    document.getElementById("aviso").className = "alert d-none";
    temporizadorAviso = null;
}

// Error dentro del modal del formulario
function mostrarError(mensaje) {
    const cartel = document.getElementById("errorFormulario");
    cartel.textContent = mensaje;
    cartel.classList.remove("d-none");
}

function ocultarError() {
    document.getElementById("errorFormulario").classList.add("d-none");
    limpiarMarcasDeCampos();
}

// Saca el borde rojo de todos los campos del formulario
function limpiarMarcasDeCampos() {
    const formulario = document.getElementById("formulario");
    if (formulario === null) {
        return;
    }
    const marcados = formulario.querySelectorAll(".is-invalid");
    for (const campo of marcados) {
        campo.classList.remove("is-invalid");
    }
}

// El backend junta TODOS los errores de validación en un solo texto con esta
// forma:  "nombre: no puede estar vacío; email: debe ser un email válido"
// Esta función lo parte y pinta cada campo. Devuelve cuántos campos marcó.
//
// La trampa: hay mensajes que NO son de validación y también tienen dos puntos,
// por ejemplo "Ya existe un dueño con cédula: 28543210". Por eso no alcanza con
// cortar en el ":": hay que comprobar que lo que quedó a la izquierda sea de
// verdad un campo del formulario. Si no lo es, no se marca nada.
function marcarErroresDeCampos(mensaje) {
    const formulario = document.getElementById("formulario");
    if (formulario === null || !mensaje) {
        return 0;
    }

    let marcados = 0;
    const partes = mensaje.split("; ");
    for (const parte of partes) {
        const corte = parte.indexOf(": ");
        if (corte === -1) {
            continue;
        }
        const nombreCampo = parte.substring(0, corte).trim();
        const textoError = parte.substring(corte + 2).trim();

        const campo = document.getElementById(nombreCampo);
        // formulario.contains(campo) es lo que descarta los falsos positivos:
        // "Ya existe un dueño con cédula" no es el id de ningún input.
        if (campo === null || !formulario.contains(campo)) {
            continue;
        }

        campo.classList.add("is-invalid");
        const cartelDelCampo = document.getElementById("error-" + nombreCampo);
        if (cartelDelCampo !== null) {
            cartelDelCampo.textContent = textoError;
        }
        marcados = marcados + 1;
    }
    return marcados;
}

// La que llaman las páginas en el catch de guardar()
function mostrarErrorDeFormulario(mensaje) {
    limpiarMarcasDeCampos();
    if (marcarErroresDeCampos(mensaje) === 0) {
        // No era un error de validación (409 de duplicado, 404, caída de la API...):
        // se muestra el mensaje completo, sin recortar.
        mostrarError(mensaje);
    } else {
        mostrarError("Revisá los campos marcados en rojo.");
    }
}


// ============================================================================
//  4. Buscador
// ============================================================================

// Convierte CUALQUIER input en un buscador. Llama a alBuscar(texto) recién
// MS_DEBOUNCE milisegundos después de la última tecla: sin eso, escribir "mariana"
// dispara siete consultas al backend, una por letra.
//
// Ojo con el temporizador: está declarado ACÁ ADENTRO, no arriba del archivo. La
// función que se le pasa al addEventListener se lo "lleva puesto" (eso es un
// closure), así que cada buscador de la página tiene el suyo. Si fuera una variable
// global, dos buscadores en la misma pantalla se cancelarían el debounce entre ellos.
function crearBuscadorEn(idCampo, alBuscar) {
    const campo = document.getElementById(idCampo);
    if (campo === null) {
        return;
    }
    let temporizador = null;

    campo.addEventListener("input", function () {
        if (temporizador !== null) {
            clearTimeout(temporizador);
        }
        temporizador = setTimeout(function () {
            alBuscar(campo.value.trim());
        }, MS_DEBOUNCE);
    });

    // Un <input> de texto adentro de un <form> manda el formulario al apretar Enter.
    // En los buscadores que viven dentro de un modal eso guardaría el registro a
    // medio cargar, así que se corta.
    campo.addEventListener("keydown", function (evento) {
        if (evento.key === "Enter") {
            evento.preventDefault();
        }
    });
}

// El buscador de la tabla de la página. Además de filtrar, se encarga de volver a la
// primera página: si eso quedara en cada pantalla, alguna se lo olvidaría.
function crearBuscador(alBuscar) {
    crearBuscadorEn("campoBuscar", function (texto) {
        // Toda búsqueda nueva empieza de cero: si estabas en la página 3 y el texto nuevo
        // devuelve una sola página, la 3 no existe y la tabla quedaría vacía.
        volverAPrimeraPagina();
        alBuscar(texto);
    });

    const botonLimpiar = document.getElementById("botonLimpiarBusqueda");
    if (botonLimpiar !== null) {
        botonLimpiar.addEventListener("click", function () {
            const campo = document.getElementById("campoBuscar");
            campo.value = "";
            volverAPrimeraPagina();
            alBuscar("");
            campo.focus();
        });
    }
}

// Llena un <select> de cero: primero lo vacía (si no, llamarla dos veces duplicaría
// todas las opciones), después pone el texto de "elegí algo..." y después una opción
// por elemento de la lista.
//
// textoDe es una función que recibe un elemento y devuelve lo que se ve en la opción.
// Se hace así porque cada combo muestra cosas distintas: una mascota se describe con
// su especie y su dueño, y un veterinario con su especialidad.
function llenarCombo(idCombo, items, textoPlaceholder, textoDe) {
    const combo = document.getElementById(idCombo);
    if (combo === null) {
        return;
    }
    combo.innerHTML = "";

    const inicial = document.createElement("option");
    inicial.value = "";
    inicial.textContent = textoPlaceholder;
    combo.appendChild(inicial);

    for (const item of items) {
        const opcion = document.createElement("option");
        opcion.value = item.id;
        // textContent y no innerHTML: el nombre lo escribió un usuario
        opcion.textContent = textoDe(item);
        combo.appendChild(opcion);
    }
}

// Arma la URL del listado de una tabla: la página y el tamaño siempre, el filtro propio
// de la pantalla si hay algo, y el orden si el usuario tocó alguna columna.
//
// nombreFiltro es un parámetro porque las pantallas no filtran igual: tres usan un texto
// ("buscar") y turnos usa un desplegable ("estado").
//
// Se empieza siempre con "?pagina=" así nunca hay que preguntarse si lo que sigue va con
// "?" o con "&", y el texto del usuario se escapa con encodeURIComponent antes de meterlo
// en la URL.
function urlDeListado(urlBase, nombreFiltro, valorFiltro) {
    let url = urlBase + "?pagina=" + paginaActual + "&tamanio=" + tamanioPagina;
    if (valorFiltro) {
        url = url + "&" + nombreFiltro + "=" + encodeURIComponent(valorFiltro);
    }
    if (campoOrden !== null) {
        url = url + "&orden=" + encodeURIComponent(campoOrden)
                + "&direccion=" + (ordenAscendente ? "asc" : "desc");
    }
    return url;
}

// La URL de un listado para llenar un <select> de un formulario.
//
// Un combo NO se pagina: o están todas las opciones que coinciden, o el usuario no puede
// elegir la que busca y encima no tiene forma de darse cuenta. Por eso pide un tamaño
// grande. No se pide un número gigante porque eso sería pedirle al servidor la tabla
// entera; el tope es acotado y, si aun así se queda corto, el combo lo avisa.
function urlParaCombo(urlBase, texto) {
    let url = urlBase + "?pagina=0&tamanio=" + TAMANIO_COMBO;
    if (texto) {
        url = url + "&buscar=" + encodeURIComponent(texto);
    }
    return url;
}

// Las filas de una respuesta paginada, SIN tocar el estado del paginador de la tabla
// (los combos no tienen paginador). Si viniera un array pelado lo devuelve tal cual: así
// sirve también para los endpoints que no se paginaron.
function contenidoDe(respuesta) {
    if (respuesta === null || respuesta === undefined) {
        return [];
    }
    if (Array.isArray(respuesta)) {
        return respuesta;
    }
    return respuesta.contenido;
}

// Cuántos hay EN TOTAL, no cuántos vinieron en esta página.
function totalDe(respuesta) {
    if (Array.isArray(respuesta)) {
        return respuesta.length;
    }
    return respuesta.totalElementos;
}

// Cada pedido de listado saca un número, como el turno de la panadería. Después del await,
// sólo el que tiene el número más alto tiene derecho a pintar la tabla: una respuesta que
// llegó tarde se descarta.
//
// Antes esto se resolvía comparando el texto buscado, pero con paginación eso ya no
// alcanza: dos clicks rápidos en "Siguiente" generan dos pedidos con el MISMO texto y
// distinta página, y si la respuesta de la primera llega última, pinta sus filas mientras
// el paginador marca la otra.
function nuevoPedido() {
    contadorPedidos = contadorPedidos + 1;
    return contadorPedidos;
}

function pedidoVigente(numero) {
    return numero === contadorPedidos;
}


// ============================================================================
//  5. Orden por columna
// ============================================================================

// Convierte en clickeables los <th> que tengan data-campo.
//
// El orden lo hace el BACKEND, no el navegador. Antes se reordenaba en memoria la lista
// ya cargada, pero con paginación eso ordenaría solamente las diez filas que estás
// viendo: la tabla diría "ordenado por apellido" habiendo ordenado 10 de 47 registros.
// No es una limitación, es un resultado incorrecto que parece correcto.
function habilitarOrdenPorColumna(alCambiarOrden) {
    const encabezados = document.querySelectorAll("th[data-campo]");
    for (const encabezado of encabezados) {
        encabezado.classList.add("ordenable");
        encabezado.setAttribute("tabindex", "0");

        const flecha = document.createElement("span");
        flecha.className = "flecha-orden";
        encabezado.appendChild(flecha);

        encabezado.addEventListener("click", function () {
            const campo = encabezado.getAttribute("data-campo");
            if (campoOrden === campo) {
                ordenAscendente = !ordenAscendente;  // segundo click: al revés
            } else {
                campoOrden = campo;
                ordenAscendente = true;
            }
            marcarFlechaOrden();
            // Cambiar el orden cambia QUÉ registros caen en cada página: la página 3
            // ordenada por cédula no tiene los mismos que la página 3 ordenada por
            // apellido. Quedarse en la 3 mostraría un pedazo del medio de una lista nueva.
            volverAPrimeraPagina();
            alCambiarOrden();
        });
    }
}

function marcarFlechaOrden() {
    const encabezados = document.querySelectorAll("th[data-campo]");
    for (const encabezado of encabezados) {
        const flecha = encabezado.querySelector(".flecha-orden");
        if (flecha === null) {
            continue;
        }
        if (encabezado.getAttribute("data-campo") === campoOrden) {
            flecha.textContent = ordenAscendente ? " ▲" : " ▼";
        } else {
            flecha.textContent = "";
        }
    }
}

// ============================================================================
//  5 bis. Paginador
// ============================================================================

// Engancha el selector de "cuántas por página" y guarda qué hacer cuando se toca un
// botón del paginador. Se llama una sola vez, desde el iniciar() de cada página.
let alCambiarPagina = null;

function habilitarPaginador(alRecargar) {
    alCambiarPagina = alRecargar;

    const selector = document.getElementById("tamanioPagina");
    if (selector !== null) {
        selector.value = String(tamanioPagina);
        selector.addEventListener("change", function () {
            tamanioPagina = parseInt(selector.value, 10);
            // Cambiar el tamaño cambia dónde caen los cortes de página: se empieza de nuevo
            volverAPrimeraPagina();
            alCambiarPagina();
        });
    }
}

// Guarda los números que vinieron en la respuesta y devuelve las filas, para que la
// página las dibuje. Es el único lugar que sabe cómo se llaman los campos de la página.
function leerPagina(respuesta) {
    paginaActual = respuesta.pagina;
    tamanioPagina = respuesta.tamanio;
    totalElementos = respuesta.totalElementos;
    totalPaginas = respuesta.totalPaginas;
    return respuesta.contenido;
}

// Si la página que se pidió ya no existe, retrocede a la última que sí y avisa que hay
// que volver a pedir.
//
// El caso típico: estás en la página 2, que tiene una sola fila, y la borrás. El backend
// devuelve una página vacía y la tabla queda en blanco con el paginador diciendo
// "Página 3 de 2". Corregirlo DESPUÉS de ver la respuesta (y no adivinando antes de
// borrar) cubre también el caso de pedir la página 5 cuando sólo hay 2.
function corregirPaginaVacia() {
    if (totalPaginas > 0 && paginaActual > totalPaginas - 1) {
        paginaActual = totalPaginas - 1;
        return true;
    }
    return false;
}

// Deja el paginador en cero. Se llama en el catch: si no, quedarían los números de la
// última respuesta buena y parecería que hay datos.
function vaciarPaginacion() {
    paginaActual = 0;
    totalPaginas = 0;
    totalElementos = 0;
}

function volverAPrimeraPagina() {
    paginaActual = 0;
}

// El número que le toca a una fila en la columna "#". Es una POSICIÓN en la lista, no un
// dato del registro: si ordenás por otra columna, al mismo dueño le toca otro número. Está
// bien que sea así, porque numera la lista, no a la persona.
//
// La cuenta considera en qué página estás: con 10 por página, la primera fila de la página 2
// es la número 11. Se puede confiar en paginaActual y tamanioPagina porque leerPagina() ya
// los dejó sincronizados con la respuesta del backend antes de que se dibuje nada.
function numeroDeFila(indice) {
    return paginaActual * tamanioPagina + indice + 1;
}

// Dibuja los botones. La llama el redibujarTabla() de cada página, al final.
function dibujarPaginador() {
    const paginador = document.getElementById("paginador");
    if (paginador === null) {
        return;
    }

    // Un paginador con un solo "1" deshabilitado es ruido: si hay una sola página, no se
    // muestra nada.
    if (totalPaginas <= 1) {
        paginador.classList.add("d-none");
        return;
    }
    paginador.classList.remove("d-none");

    document.getElementById("resumenPaginacion").textContent =
            "Página " + (paginaActual + 1) + " de " + totalPaginas;

    const lista = document.getElementById("listaPaginas");
    lista.innerHTML = "";

    lista.appendChild(crearBotonPagina("‹ Anterior", paginaActual - 1, false,
            paginaActual === 0, "Página anterior"));

    // Ventana deslizante: con 50 páginas se dibujan 5 botones, no 50. Lo que la ventana
    // esconde lo dice el resumen de arriba ("Página 23 de 50"), por eso ese texto no es
    // decorativo: es el que hace que mostrar sólo cinco números sea correcto.
    let primera = paginaActual - Math.floor(MAX_BOTONES_PAGINA / 2);
    if (primera < 0) {
        primera = 0;
    }
    let ultima = primera + MAX_BOTONES_PAGINA - 1;
    if (ultima > totalPaginas - 1) {
        ultima = totalPaginas - 1;
        primera = ultima - MAX_BOTONES_PAGINA + 1;
        if (primera < 0) {
            primera = 0;
        }
    }
    for (let numero = primera; numero <= ultima; numero = numero + 1) {
        lista.appendChild(crearBotonPagina(String(numero + 1), numero,
                numero === paginaActual, false, "Página " + (numero + 1)));
    }

    lista.appendChild(crearBotonPagina("Siguiente ›", paginaActual + 1, false,
            paginaActual >= totalPaginas - 1, "Página siguiente"));
}

function crearBotonPagina(texto, numeroPagina, activo, deshabilitado, etiquetaAria) {
    const item = document.createElement("li");
    item.className = "page-item";
    if (activo) {
        item.classList.add("active");
    }
    if (deshabilitado) {
        item.classList.add("disabled");
    }

    const boton = document.createElement("button");
    // type="button" porque un <button> sin type adentro de un <form> hace submit, y
    // <button> en vez de <a href="#"> porque el ancla salta al tope de la página.
    boton.type = "button";
    boton.className = "page-link";
    boton.textContent = texto;
    boton.setAttribute("aria-label", etiquetaAria);

    if (deshabilitado) {
        // La clase de Bootstrap sólo lo pinta gris: sin esta propiedad el click sigue
        // funcionando y "Anterior" en la página 0 pediría la página -1.
        boton.disabled = true;
    } else if (activo) {
        // Al botón de la página en la que estás no se le engancha nada: tocarlo no tiene
        // que gastar un pedido al backend. No se lo deshabilita para no perder el resaltado.
        boton.setAttribute("aria-current", "page");
    } else {
        boton.addEventListener("click", function () {
            paginaActual = numeroPagina;
            alCambiarPagina();
        });
    }

    item.appendChild(boton);
    return item;
}

// ============================================================================
//  6. Panel lateral de detalle (offcanvas de Bootstrap)
// ============================================================================

// campos es una lista de pares: [["Nombre", "Ana"], ["Cédula", "28543210"]]
function abrirPanel(titulo, campos) {
    if (panelDetalle === null) {
        return;
    }
    document.getElementById("tituloPanel").textContent = titulo;

    const ficha = document.getElementById("fichaPanel");
    ficha.innerHTML = "";
    for (const campo of campos) {
        const etiqueta = document.createElement("dt");
        etiqueta.className = "col-5 text-muted fw-normal small";
        etiqueta.textContent = campo[0];

        const valor = document.createElement("dd");
        valor.className = "col-7 small mb-1";
        valor.textContent = (campo[1] === null || campo[1] === undefined || campo[1] === "")
            ? "—" : campo[1];

        ficha.appendChild(etiqueta);
        ficha.appendChild(valor);
    }

    document.getElementById("listaRelacionados").innerHTML = "";
    document.getElementById("sinRelacionados").classList.add("d-none");
    document.getElementById("avisoPanel").classList.add("d-none");
    panelDetalle.show();
}

function cerrarPanel() {
    // Se llama antes de abrir un modal: Bootstrap apila los fondos oscuros del
    // offcanvas y del modal, y al cerrarlos el body puede quedar sin scroll.
    if (panelDetalle !== null) {
        panelDetalle.hide();
    }
}

// Título de la sección de abajo del panel + spinner mientras se pide la lista
function mostrarRelacionados(titulo) {
    document.getElementById("tituloRelacionados").textContent = titulo;
    document.getElementById("cargandoPanel").classList.remove("d-none");
    document.getElementById("listaRelacionados").innerHTML = "";
    document.getElementById("sinRelacionados").classList.add("d-none");
}

function agregarItemPanel(principal, secundario, textoBadge, claseBadge) {
    const item = document.createElement("li");
    item.className = "list-group-item px-0 d-flex justify-content-between align-items-start gap-2";

    const textos = document.createElement("div");
    const fuerte = document.createElement("div");
    fuerte.className = "fw-semibold small";
    fuerte.textContent = principal;
    textos.appendChild(fuerte);

    if (secundario) {
        const suave = document.createElement("div");
        suave.className = "text-muted small";
        suave.textContent = secundario;
        textos.appendChild(suave);
    }
    item.appendChild(textos);

    if (textoBadge) {
        const etiqueta = document.createElement("span");
        etiqueta.className = "badge " + (claseBadge ? claseBadge : "text-bg-secondary");
        etiqueta.textContent = textoBadge;
        item.appendChild(etiqueta);
    }

    document.getElementById("listaRelacionados").appendChild(item);
}

// Apaga el spinner. Si no había nada que mostrar, aclara que está vacío: una
// lista vacía NO es un error.
function terminarRelacionados(cantidad, textoVacio) {
    document.getElementById("cargandoPanel").classList.add("d-none");
    if (cantidad === 0) {
        const cartel = document.getElementById("sinRelacionados");
        cartel.textContent = textoVacio;
        cartel.classList.remove("d-none");
    }
}

// Un error al cargar lo relacionado se muestra DENTRO del panel: el panel no se
// cierra y la tabla de atrás sigue usable.
function mostrarAvisoPanel(mensaje) {
    document.getElementById("cargandoPanel").classList.add("d-none");
    const cartel = document.getElementById("avisoPanel");
    cartel.textContent = mensaje;
    cartel.classList.remove("d-none");
}


// ============================================================================
//  7. Formato de datos
// ============================================================================

// "2026-03-15" → "15/03/2026". Se parte el texto a mano en vez de usar Date
// porque new Date("2026-03-15") se interpreta en UTC y, con nuestro huso, puede
// mostrar el día anterior.
function fechaCorta(fecha) {
    if (!fecha) {
        return "";
    }
    const partes = fecha.split("-");
    if (partes.length !== 3) {
        return fecha;
    }
    return partes[2] + "/" + partes[1] + "/" + partes[0];
}

// "10:30:00" → "10:30"
function horaCorta(hora) {
    if (!hora) {
        return "";
    }
    return hora.substring(0, 5);
}

// La fecha de hoy en formato ISO, armada a mano por la misma razón que fechaCorta
function fechaDeHoy() {
    const hoy = new Date();
    const mes = String(hoy.getMonth() + 1).padStart(2, "0");
    const dia = String(hoy.getDate()).padStart(2, "0");
    return hoy.getFullYear() + "-" + mes + "-" + dia;
}

function colorDelEstado(estado) {
    if (estado === "CONFIRMADO") {
        return "text-bg-primary";
    }
    if (estado === "ATENDIDO") {
        return "text-bg-success";
    }
    if (estado === "CANCELADO") {
        return "text-bg-danger";
    }
    return "text-bg-secondary";  // PENDIENTE
}

// Emoji en vez de una librería de iconos: no agrega ninguna dependencia nueva
// y se puede escribir con textContent, que es seguro.
function iconoDeEspecie(especie) {
    const texto = (especie ? especie : "").toLowerCase();
    if (texto.indexOf("perr") === 0 || texto.indexOf("can") === 0) {
        return "🐶";
    }
    if (texto.indexOf("gat") === 0 || texto.indexOf("fel") === 0) {
        return "🐱";
    }
    if (texto.indexOf("conej") === 0) {
        return "🐰";
    }
    if (texto.indexOf("av") === 0 || texto.indexOf("pajar") === 0 || texto.indexOf("loro") === 0) {
        return "🐦";
    }
    if (texto.indexOf("tortug") === 0 || texto.indexOf("reptil") === 0) {
        return "🐢";
    }
    if (texto.indexOf("hamst") === 0 || texto.indexOf("raton") === 0) {
        return "🐹";
    }
    if (texto.indexOf("caball") === 0 || texto.indexOf("equin") === 0) {
        return "🐴";
    }
    return "🐾";
}


// ============================================================================
//  8. Arranque del núcleo — lo llama el iniciar() de cada página
// ============================================================================

function iniciarNucleo() {
    const panel = document.getElementById("panelDetalle");
    if (panel !== null) {
        panelDetalle = new bootstrap.Offcanvas(panel);
    }

    // Mientras el usuario corrige un campo marcado en rojo, se le saca la marca:
    // si no, el borde rojo queda hasta que vuelve a guardar.
    const formulario = document.getElementById("formulario");
    if (formulario !== null) {
        formulario.addEventListener("input", function (evento) {
            evento.target.classList.remove("is-invalid");
        });
    }
}
