package com.vetSystem.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Personaliza la portada de Swagger UI (título, descripción, versión y contacto).
// Los endpoints NO se declaran acá: springdoc los descubre solo leyendo los
// @RestController y las anotaciones @Tag / @Operation de cada uno.
//   Documentación interactiva: http://localhost:8080/swagger-ui.html
//   Especificación en JSON:    http://localhost:8080/v3/api-docs
@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI vetSystemOpenAPI() {
        Info info = new Info()
                .title("API Clínica Veterinaria \"Patitas Felices\"")
                .description("API REST del sistema de gestión de la clínica: dueños, mascotas, "
                        + "veterinarios y turnos. Trabajo Práctico de Microservicios y APIs "
                        + "Escalables (Universidad de Palermo, 2C 2026).")
                .version("1.0")
                .contact(new Contact()
                        .name("Esteban Castañera")
                        .email("estebancastanera@gmail.com"))
                .license(new License().name("Uso académico"));

        return new OpenAPI().info(info);
    }
}
