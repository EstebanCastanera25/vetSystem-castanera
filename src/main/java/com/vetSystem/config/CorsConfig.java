package com.vetSystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

// CORS: el navegador bloquea las peticiones entre orígenes distintos. El frontend
// se sirve en el puerto 5500 y la API en el 8080, así que para el navegador son
// dos orígenes diferentes y sin este permiso el fetch nunca llega.
// Postman no tiene este problema: CORS es una política del navegador, no del servidor.
//
// OJO: el origen tiene que coincidir EXACTO. Si el HTML se abre con doble clic
// (protocolo file://) el origen es "null" y la petición se rechaza igual.
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                        "http://localhost:5500",
                        "http://127.0.0.1:5500")
                // PATCH está incluido porque lo usa el cambio de estado de un turno
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }
}
