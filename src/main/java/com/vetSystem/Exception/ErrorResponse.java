package com.vetSystem.Exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

// Estructura estándar de TODAS las respuestas de error de la API.
// El cliente puede confiar en que un error siempre trae estos cinco campos.
@Schema(description = "Respuesta estándar para los errores de la API")
@Data
@AllArgsConstructor
public class ErrorResponse {

    @Schema(description = "Fecha y hora en que ocurrió el error",
            example = "2026-09-20T14:30:15.123", accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDateTime timestamp;
    @Schema(description = "Código de estado HTTP del error", example = "404",
            accessMode = Schema.AccessMode.READ_ONLY)
    private int status;
    @Schema(description = "Nombre del estado HTTP", example = "Not Found",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String error;
    @Schema(description = "Mensaje descriptivo del error",
            example = "Duenio con id 99 no fue encontrado", accessMode = Schema.AccessMode.READ_ONLY)
    private String mensaje;
    @Schema(description = "Ruta de la solicitud que produjo el error", example = "/api/duenios/99",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String path;
}
