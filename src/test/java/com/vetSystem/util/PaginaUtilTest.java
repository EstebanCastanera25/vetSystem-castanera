package com.vetSystem.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests del recorte de parametros y del armado del orden.
 *
 * <p>Cada uno de estos casos, sin el recorte, es un HTTP 500 que cualquiera puede disparar
 * cambiando la URL: PageRequest.of() lanza IllegalArgumentException con una pagina negativa
 * o un tamanio cero, y Hibernate falla al armar la consulta si el campo de orden no existe.
 */
@DisplayName("Tests unitarios de PaginaUtil")
class PaginaUtilTest {

    private static final Sort ORDEN_POR_DEFECTO =
            Sort.by(Sort.Order.asc("apellido"), Sort.Order.asc("id"));
    private static final Map<String, String> CAMPOS_PERMITIDOS =
            Map.of("nombre", "nombre", "apellido", "apellido", "duenioNombre", "duenio.nombre");

    @Test
    @DisplayName("Sin parametros usa la pagina 0 y el tamanio por defecto")
    void armarPageable_cuandoNoVienenParametros_usaLosValoresPorDefecto() {
        // ARRANGE + ACT
        Pageable resultado = PaginaUtil.armarPageable(null, null, ORDEN_POR_DEFECTO);

        // ASSERT
        assertThat(resultado.getPageNumber()).isEqualTo(0);
        assertThat(resultado.getPageSize()).isEqualTo(PaginaUtil.TAMANIO_POR_DEFECTO);
    }

    @Test
    @DisplayName("Un tamanio de cero usa el tamanio por defecto en vez de reventar")
    void armarPageable_cuandoElTamanioEsCero_usaElTamanioPorDefecto() {
        // ARRANGE + ACT: PageRequest.of(0, 0) lanza "Page size must not be less than one"
        Pageable resultado = PaginaUtil.armarPageable(0, 0, ORDEN_POR_DEFECTO);

        // ASSERT
        assertThat(resultado.getPageSize()).isEqualTo(PaginaUtil.TAMANIO_POR_DEFECTO);
    }

    @Test
    @DisplayName("Un tamanio enorme se recorta al maximo")
    void armarPageable_cuandoElTamanioSuperaElMaximo_loRecorta() {
        // ARRANGE + ACT
        Pageable resultado = PaginaUtil.armarPageable(0, 99999, ORDEN_POR_DEFECTO);

        // ASSERT: proteger la base de un pedido que traeria la tabla entera
        assertThat(resultado.getPageSize()).isEqualTo(PaginaUtil.TAMANIO_MAXIMO);
    }

    @Test
    @DisplayName("Una pagina negativa se recorta a la cero en vez de reventar")
    void armarPageable_cuandoLaPaginaEsNegativa_usaLaCero() {
        // ARRANGE + ACT: PageRequest.of(-1, 10) lanza "Page index must not be less than zero"
        Pageable resultado = PaginaUtil.armarPageable(-1, 10, ORDEN_POR_DEFECTO);

        // ASSERT
        assertThat(resultado.getPageNumber()).isEqualTo(0);
    }

    @Test
    @DisplayName("Un campo de orden permitido se usa, con el id como desempate")
    void armarOrden_cuandoElCampoEstaPermitido_ordenaPorEseCampo() {
        // ARRANGE + ACT
        Sort resultado = PaginaUtil.armarOrden("nombre", "desc", CAMPOS_PERMITIDOS, ORDEN_POR_DEFECTO);

        // ASSERT
        assertThat(resultado.getOrderFor("nombre")).isNotNull();
        assertThat(resultado.getOrderFor("nombre").isDescending()).isTrue();
        // El id va siempre al final: sin desempate, dos filas iguales pueden cambiar de
        // lugar entre una pagina y otra, y una podria aparecer dos veces o ninguna.
        assertThat(resultado.getOrderFor("id")).isNotNull();
        assertThat(resultado.getOrderFor("id").isAscending()).isTrue();
    }

    @Test
    @DisplayName("Un campo de orden traducido apunta al campo real de la entidad")
    void armarOrden_cuandoElCampoSeTraduce_usaLaRutaDeLaEntidad() {
        // ARRANGE + ACT: la tabla muestra una columna "duenioNombre", pero en la entidad
        // ese dato vive en duenio.nombre
        Sort resultado = PaginaUtil.armarOrden("duenioNombre", "asc", CAMPOS_PERMITIDOS, ORDEN_POR_DEFECTO);

        // ASSERT
        assertThat(resultado.getOrderFor("duenio.nombre")).isNotNull();
        assertThat(resultado.getOrderFor("duenioNombre")).isNull();
    }

    @Test
    @DisplayName("Un campo de orden que no existe cae al orden por defecto, no a un 500")
    void armarOrden_cuandoElCampoNoEstaPermitido_usaElOrdenPorDefecto() {
        // ARRANGE + ACT: sin la lista de permitidos, Sort.by("chau") hace que Hibernate
        // falle al parsear la consulta y la API devuelva un 500
        Sort resultado = PaginaUtil.armarOrden("chau", "asc", CAMPOS_PERMITIDOS, ORDEN_POR_DEFECTO);

        // ASSERT
        assertThat(resultado).isEqualTo(ORDEN_POR_DEFECTO);
    }

    @Test
    @DisplayName("Sin campo de orden usa el orden por defecto")
    void armarOrden_cuandoNoSePideOrden_usaElOrdenPorDefecto() {
        // ARRANGE + ACT + ASSERT
        assertThat(PaginaUtil.armarOrden(null, null, CAMPOS_PERMITIDOS, ORDEN_POR_DEFECTO))
                .isEqualTo(ORDEN_POR_DEFECTO);
        assertThat(PaginaUtil.armarOrden("   ", "asc", CAMPOS_PERMITIDOS, ORDEN_POR_DEFECTO))
                .isEqualTo(ORDEN_POR_DEFECTO);
    }
}
