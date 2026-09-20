package com.vetSystem.Exception;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// Estructura estándar de TODAS las respuestas de error de la API.
// El cliente puede confiar en que un error siempre trae estos cinco campos.
@Data
@AllArgsConstructor
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String path;
}
