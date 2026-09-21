package com.vetSystem.util;

import com.vetSystem.DTO.PaginaDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Map;

/**
 * Todo lo que hace falta para paginar, en un solo lugar.
 *
 * <p>Es el ÚNICO archivo del proyecto que conoce {@code Pageable}, {@code PageRequest} y
 * {@code Sort} además de los repositorios. Los services le piden el pedido de página y la
 * traducción a {@link PaginaDTO}, y así la clase de Spring no se filtra ni a los controllers
 * ni a la respuesta JSON.
 *
 * <p>Mismo criterio que {@link TextoUtil}: métodos estáticos, sin estado, sin dependencias,
 * y un solo lugar que decide la regla.
 */
public final class PaginaUtil {

    /** Cuántos elementos trae una página si el cliente no pide un tamaño. */
    public static final int TAMANIO_POR_DEFECTO = 10;

    /**
     * Tope de elementos por página. Es 200 porque los desplegables de los formularios
     * piden ese tamaño para traer todas sus opciones de una; más que eso es pedirle al
     * servidor que devuelva la tabla entera.
     */
    public static final int TAMANIO_MAXIMO = 200;

    private PaginaUtil() {
    }

    /**
     * Arma el pedido de página, recortando lo que venga fuera de rango.
     *
     * <p>El recorte NO es decorativo: {@code PageRequest.of(-1, 10)} y
     * {@code PageRequest.of(0, 0)} lanzan {@code IllegalArgumentException}, que sin este
     * recorte terminaría en un HTTP 500 disparable desde la URL.
     *
     * <p>La regla del proyecto queda en una frase: <b>400 si el parámetro no es un número</b>
     * (de eso se encarga Spring al convertirlo), <b>recorte silencioso si es un número fuera
     * de rango</b>. Es la misma doctrina del buscador: un filtro opcional nunca falla.
     */
    public static Pageable armarPageable(Integer pagina, Integer tamanio, Sort orden) {
        int numeroDePagina = (pagina == null || pagina < 0) ? 0 : pagina;

        int elementosPorPagina;
        if (tamanio == null || tamanio < 1) {
            elementosPorPagina = TAMANIO_POR_DEFECTO;
        } else if (tamanio > TAMANIO_MAXIMO) {
            elementosPorPagina = TAMANIO_MAXIMO;
        } else {
            elementosPorPagina = tamanio;
        }

        return PageRequest.of(numeroDePagina, elementosPorPagina, orden);
    }

    /**
     * Arma el orden a partir de lo que pidió el cliente.
     *
     * <p>{@code camposPermitidos} traduce el nombre que manda el frontend al campo real de
     * la entidad. Sirve para dos cosas:
     *
     * <ul>
     *   <li><b>Traducir</b>: la tabla de turnos ordena por una columna que llama
     *       "mascotaNombre", pero en la entidad ese dato es {@code mascota.nombre}.</li>
     *   <li><b>Proteger</b>: un campo que no existe hace que Hibernate falle al armar la
     *       consulta y la API devuelva un 500 que cualquiera puede disparar cambiando la
     *       URL. Lo que no está en la lista cae al orden por defecto.</li>
     * </ul>
     *
     * <p>El orden por defecto de cada entidad termina SIEMPRE en el id. Sin un orden total
     * y determinístico, la base puede devolver una misma fila en dos páginas distintas, o
     * en ninguna: paginar sin orden estable está mal, aunque a simple vista funcione.
     */
    public static Sort armarOrden(String orden, String direccion,
                                  Map<String, String> camposPermitidos, Sort ordenPorDefecto) {
        if (orden == null || orden.isBlank()) {
            return ordenPorDefecto;
        }
        String campoReal = camposPermitidos.get(orden.trim());
        if (campoReal == null) {
            return ordenPorDefecto;
        }

        boolean descendente = "desc".equalsIgnoreCase(direccion);
        Sort.Order ordenPedido = descendente
                ? Sort.Order.desc(campoReal)
                : Sort.Order.asc(campoReal);

        // El id va siempre al final como desempate: dos dueños con el mismo apellido
        // tienen que quedar siempre en el mismo orden relativo entre una página y otra.
        return Sort.by(ordenPedido, Sort.Order.asc("id"));
    }

    /**
     * Traduce la página de Spring a la respuesta de la API.
     *
     * <p>El contenido ya viene convertido a DTO por el service (que es el único que sabe
     * mapear cada entidad); acá se le agregan los números de la página.
     */
    public static <T> PaginaDTO<T> armar(List<T> contenido, Page<?> pagina) {
        return new PaginaDTO<>(
                contenido,
                pagina.getNumber(),
                pagina.getSize(),
                pagina.getTotalElements(),
                pagina.getTotalPages());
    }
}
