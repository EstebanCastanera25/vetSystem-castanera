package com.vetSystem.Controller;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.Service.VeterinarioService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests del contrato HTTP del alta de veterinarios.
 *
 * <p>Lo que se prueba aca es la VALIDACION del formato de la matricula, que corre en la capa
 * web (el @Valid del controller) y no en el service. Por eso hace falta un @WebMvcTest: en un
 * test de service con Mockito, Bean Validation ni siquiera se ejecuta.
 *
 * <p>Es el unico lugar que cubre el flag CASE_INSENSITIVE del @Pattern de VeterinarioDTO.
 */
@WebMvcTest(VeterinarioController.class)
@DisplayName("Tests del contrato HTTP de VeterinarioController")
class VeterinarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VeterinarioService veterinarioService;

    private VeterinarioDTO veterinarioValido(String matricula) {
        return new VeterinarioDTO(null, "Leandro", "Perez", matricula, "Clinica General",
                "leandro@vet.com");
    }

    @Test
    @DisplayName("POST con la matricula en minusculas devuelve 201: el service la normaliza")
    void createVeterinario_cuandoLaMatriculaVieneEnMinusculas_retorna201() throws Exception {
        // ARRANGE: el service devuelve el veterinario ya con la matricula normalizada
        when(veterinarioService.registrarEntidad(any(VeterinarioDTO.class)))
                .thenReturn(new VeterinarioDTO(1L, "Leandro", "Perez", "MV-9999",
                        "Clinica General", "leandro@vet.com"));

        // ACT + ASSERT: antes del flag CASE_INSENSITIVE, el @Pattern rechazaba esto con un
        // 400 y el pedido nunca llegaba al service.
        mockMvc.perform(post("/api/veterinarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veterinarioValido("mv-9999"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.matricula").value("MV-9999"));
    }

    @Test
    @DisplayName("POST con una matricula de otro formato sigue devolviendo 400")
    void createVeterinario_cuandoLaMatriculaTieneOtroFormato_retorna400() throws Exception {
        // ACT + ASSERT: aceptar minusculas NO es aceptar cualquier cosa. El formato
        // MV-numero se sigue exigiendo.
        mockMvc.perform(post("/api/veterinarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(veterinarioValido("XX-99"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.mensaje", containsString("matricula")));

        // El pedido se corta en la capa web: el service ni se entera
        verifyNoInteractions(veterinarioService);
    }
}
