package com.vetSystem.Controller;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests de integracion (parciales) de la capa web.
 *
 * <p>{@code @WebMvcTest} levanta SOLO la porcion web del contexto de Spring: el
 * controller indicado, el {@code GlobalExceptionHandler} (los {@code @ControllerAdvice}
 * entran en el slice) y la validacion de Bean Validation. NO levanta Services,
 * Repositories ni el DataSource, asi que estos tests no tocan ninguna base de datos
 * ni necesitan un servidor corriendo.
 *
 * <p>Lo que se prueba aca es el contrato HTTP: rutas, codigos de estado y forma del
 * JSON. La logica de negocio se prueba en los tests de Service.
 */
@WebMvcTest(DuenioController.class)
@DisplayName("DuenioController - contrato HTTP con MockMvc")
class DuenioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ObjectMapper de Jackson 3 (paquete tools.jackson): en Spring Boot 4 ya no
    // existe com.fasterxml.jackson.databind en el classpath.
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DuenioService duenioService;

    // DuenioController tambien inyecta MascotaService (endpoint /{id}/mascotas).
    // Sin este mock el contexto del slice no arranca.
    @MockitoBean
    private MascotaService mascotaService;

    private DuenioDTO duenioValido(Long id) {
        return new DuenioDTO(id, "Carlos", "Sanchez", "31541741", 1155667788, "carlos@mail.com");
    }

    @Test
    @DisplayName("GET /api/duenios sin datos devuelve 200 y una lista vacia")
    void getAllDuenios_cuandoNoHayDuenios_retorna200YListaVacia() throws Exception {
        // ARRANGE
        when(duenioService.listarEntidades()).thenReturn(List.of());

        // ACT + ASSERT
        mockMvc.perform(get("/api/duenios"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/duenios/{id} de un duenio existente devuelve 200 y sus datos")
    void getDuenioById_cuandoExiste_retorna200YElDuenio() throws Exception {
        // ARRANGE
        when(duenioService.buscarPorId(1L)).thenReturn(Optional.of(duenioValido(1L)));

        // ACT + ASSERT
        mockMvc.perform(get("/api/duenios/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"))
                .andExpect(jsonPath("$.cedula").value("31541741"));
    }

    @Test
    @DisplayName("GET /api/duenios/{id} inexistente devuelve 404 con el ErrorResponse")
    void getDuenioById_cuandoNoExiste_retorna404() throws Exception {
        // ARRANGE: el Service devuelve Optional vacio; es el Controller el que
        // lanza ResourceNotFoundException, y el GlobalExceptionHandler la traduce a 404.
        when(duenioService.buscarPorId(99L)).thenReturn(Optional.empty());

        // ACT + ASSERT
        mockMvc.perform(get("/api/duenios/{id}", 99L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                // el campo del ErrorResponse es "mensaje" (en castellano), no "message"
                .andExpect(jsonPath("$.mensaje").value("Duenio con id 99 no fue encontrado"))
                .andExpect(jsonPath("$.path").value("/api/duenios/99"));
    }

    @Test
    @DisplayName("POST /api/duenios con datos validos devuelve 201 y el duenio creado")
    void createDuenio_cuandoDatosValidos_retorna201() throws Exception {
        // ARRANGE
        DuenioDTO entrada = duenioValido(null);
        when(duenioService.registrarEntidad(any(DuenioDTO.class))).thenReturn(duenioValido(1L));

        // ACT + ASSERT
        mockMvc.perform(post("/api/duenios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.nombre").value("Carlos"));
    }

    @Test
    @DisplayName("POST /api/duenios con email vacio devuelve 400 y no llega al Service")
    void createDuenio_cuandoEmailVacio_retorna400() throws Exception {
        // ARRANGE: email vacio viola @NotBlank -> MethodArgumentNotValidException
        DuenioDTO invalido = new DuenioDTO(null, "Carlos", "Sanchez", "31541741", 1155667788, "");

        // ACT + ASSERT
        mockMvc.perform(post("/api/duenios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                // el orden de los field errors no esta garantizado: se busca el campo, no el texto exacto
                .andExpect(jsonPath("$.mensaje", containsString("email")));

        // La validacion corta ANTES del Service: la request invalida nunca llega a la logica de negocio.
        verifyNoInteractions(duenioService);
    }

    @Test
    @DisplayName("POST /api/duenios con cedula ya registrada devuelve 409")
    void createDuenio_cuandoCedulaDuplicada_retorna409() throws Exception {
        // ARRANGE
        DuenioDTO entrada = duenioValido(null);
        when(duenioService.registrarEntidad(any(DuenioDTO.class)))
                .thenThrow(new DuplicateResourceException("Ya existe un dueño con cédula: 31541741"));

        // ACT + ASSERT
        mockMvc.perform(post("/api/duenios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(entrada)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.mensaje", containsString("31541741")));
    }
}
