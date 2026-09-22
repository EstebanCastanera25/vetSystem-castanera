package com.vetSystem;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifica que la documentacion de OpenAPI se genere de verdad.
 *
 * <p>Por que hace falta este test: springdoc arma la especificacion la PRIMERA vez que
 * alguien pide /v3/api-docs, no al arrancar. Por eso el {@code contextLoads} puede pasar
 * en verde aunque la generacion este rota (por ejemplo, si una anotacion quedo mal o si
 * la version de springdoc no es compatible con esta version de Spring Boot). Este test
 * pide el JSON y comprueba que tenga los 4 recursos documentados.
 *
 * <p>Corre sobre H2, igual que el resto: no necesita MySQL ni un servidor levantado.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Documentacion OpenAPI generada por springdoc")
class SwaggerDocsTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET /v3/api-docs devuelve la especificacion con los endpoints y los @Tag")
    void apiDocs_devuelveLaEspecificacionCompleta() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                // los metadatos que define SwaggerConfig
                .andExpect(jsonPath("$.openapi").isString())
                .andExpect(jsonPath("$.info.version").value("1.0"))
                // los 4 recursos de la clinica, cada uno con su @Tag
                .andExpect(jsonPath("$.paths['/api/duenios']").exists())
                .andExpect(jsonPath("$.paths['/api/mascotas']").exists())
                .andExpect(jsonPath("$.paths['/api/veterinarios']").exists())
                .andExpect(jsonPath("$.paths['/api/turnos']").exists())
                .andExpect(jsonPath("$.paths['/api/duenios'].get.tags[0]").value("Dueños"))
                // el buscador figura como parametro opcional del listado, no como un path aparte
                .andExpect(jsonPath("$.paths['/api/duenios'].get.parameters[0].name").value("buscar"))
                .andExpect(jsonPath("$.paths['/api/duenios'].get.parameters[0].required").value(false))
                // los @Schema de los DTOs: descripcion y ejemplo de cada campo
                .andExpect(jsonPath("$.components.schemas.DuenioDTO.properties.cedula.example")
                        .value("28543210"))
                // y los @ApiResponse de error apuntan al ErrorResponse, no al DTO de exito
                .andExpect(jsonPath("$.components.schemas.ErrorResponse").exists());
    }
}
