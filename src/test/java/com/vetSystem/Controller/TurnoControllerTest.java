package com.vetSystem.Controller;

import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Service.TurnoService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests del filtro por estado del listado de turnos.
 *
 * <p>{@code @WebMvcTest} levanta SOLO la capa web: el controller, el
 * {@code GlobalExceptionHandler} y la conversion de parametros. El service va
 * mockeado, asi que estos tests no tocan la base ni necesitan el servidor.
 */
@WebMvcTest(TurnoController.class)
@DisplayName("Tests del contrato HTTP de TurnoController")
class TurnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TurnoService turnoService;

    private TurnoResponseDTO turnoValido(Long id, EstadoTurno estado) {
        return new TurnoResponseDTO(id, LocalDate.of(2026, 10, 1), LocalTime.of(10, 30),
                "Vacunacion", estado, null, 1L, "Firulais", 1L, "Leandro");
    }

    @Test
    @DisplayName("GET /api/turnos sin filtro devuelve 200 y todos los turnos")
    void listarTurnos_cuandoNoSeFiltra_retorna200YTodosLosTurnos() throws Exception {
        // ARRANGE: sin el parametro estado, Spring le pasa null al controller
        when(turnoService.listarTurnos(null, null, null, null, null))
                .thenReturn(new PaginaDTO<>(
                        List.of(turnoValido(1L, EstadoTurno.PENDIENTE),
                                turnoValido(2L, EstadoTurno.ATENDIDO)),
                        0, 10, 2, 1));

        // ACT + ASSERT
        mockMvc.perform(get("/api/turnos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/turnos?estado=PENDIENTE devuelve 200 y solo los de ese estado")
    void listarTurnos_cuandoSeFiltraPorEstado_retorna200YSoloLosDeEseEstado() throws Exception {
        // ARRANGE: Spring convierte el texto de la URL al enum EstadoTurno
        when(turnoService.listarTurnos(EstadoTurno.PENDIENTE, null, null, null, null))
                .thenReturn(new PaginaDTO<>(List.of(turnoValido(1L, EstadoTurno.PENDIENTE)),
                        0, 10, 1, 1));

        // ACT + ASSERT
        mockMvc.perform(get("/api/turnos").param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(1)))
                .andExpect(jsonPath("$.contenido[0].estado").value("PENDIENTE"));
    }

    @Test
    @DisplayName("Un estado que no existe devuelve 400, no 500")
    void listarTurnos_cuandoElEstadoNoEsValido_retorna400() throws Exception {
        // ACT + ASSERT: "BASURA" no es un valor del enum, asi que la conversion falla antes
        // de llegar al controller. El error es del PEDIDO, no del servidor: tiene que ser 400.
        // Sin el handler de MethodArgumentTypeMismatchException esto caia en el 500 generico.
        mockMvc.perform(get("/api/turnos").param("estado", "BASURA"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("estado")));

        // Y el service ni se entera: el pedido se corta en la capa web
        verifyNoInteractions(turnoService);
    }

    @Test
    @DisplayName("Una busqueda sin resultados devuelve 200 y lista vacia, NO 404")
    void listarTurnos_cuandoNoHayTurnosEnEseEstado_retorna200YListaVacia() throws Exception {
        // ARRANGE
        when(turnoService.listarTurnos(EstadoTurno.CANCELADO, null, null, null, null))
                .thenReturn(new PaginaDTO<>(List.of(), 0, 10, 0, 0));

        // ACT + ASSERT
        mockMvc.perform(get("/api/turnos").param("estado", "CANCELADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.contenido", hasSize(0)));
    }
}
