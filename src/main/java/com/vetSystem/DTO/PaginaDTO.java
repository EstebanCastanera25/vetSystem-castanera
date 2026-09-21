package com.vetSystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Una página de resultados de un listado.
 *
 * <p>Es la respuesta de los cuatro listados de la API. El requisito RF-02 del SRS pide
 * que {@code GET /api/duenios} devuelva una lista paginada; acá está su forma:
 *
 * <pre>
 * {"contenido": [...], "pagina": 0, "tamanio": 10, "totalElementos": 47, "totalPaginas": 5}
 * </pre>
 *
 * <p><b>Por qué una clase propia y no el {@code Page} de Spring Data.</b> Spring tiene su
 * propia clase de página y se podría devolver directamente, pero esta clase no importa
 * NADA de Spring, a propósito, por dos razones:
 *
 * <ul>
 *   <li>El JSON de esta API está en castellano: el error se llama {@code mensaje} y no
 *       {@code message}. El {@code Page} de Spring devolvería {@code content},
 *       {@code totalElements} y una docena de campos más que nadie pidió.</li>
 *   <li>El proyecto ya defiende que <i>la entidad JPA no sale del service</i>. Por el mismo
 *       criterio tampoco sale una clase de la librería de persistencia: la forma de la
 *       respuesta es un contrato nuestro, no un detalle de la herramienta.</li>
 * </ul>
 *
 * <p>El {@code Page} de Spring existe igual, pero muere adentro del service: lo traduce
 * {@link com.vetSystem.util.PaginaUtil}.
 *
 * @param <T> el tipo de los elementos de la página (DuenioDTO, MascotaDTO, ...)
 */
@Schema(description = "Una página de resultados: los elementos y en qué parte del total están")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaginaDTO<T> {

    @Schema(description = "Los elementos de esta página")
    private List<T> contenido;

    @Schema(description = "Número de página, empezando en 0", example = "0")
    private int pagina;

    @Schema(description = "Cuántos elementos trae cada página", example = "10")
    private int tamanio;

    @Schema(description = "Cuántos elementos hay en total, sumando todas las páginas",
            example = "47")
    private long totalElementos;

    @Schema(description = "Cuántas páginas hay en total", example = "5")
    private int totalPaginas;
}
