package com.vetSystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Datos de un dueño de la clínica veterinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DuenioDTO {

    @Schema(description = "Identificador único del dueño, generado por el servidor",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(description = "Nombre de pila del dueño", example = "Maria")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @Schema(description = "Apellido del dueño", example = "Gomez")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @Schema(description = "Número de cédula del dueño", example = "28543210")
    @NotBlank(message = "La cédula es obligatoria")
    @Size(min = 7, max = 8, message = "La cédula debe tener entre 7 y 8 dígitos")
    private String cedula;
    // La columna es NOT NULL: sin esta validación un teléfono vacío llegaba a la base y daba 500
    @Schema(description = "Número de teléfono del dueño", example = "1144556677")
    @NotNull(message = "El teléfono es obligatorio")
    private Integer telefono;
    @Schema(description = "Correo electrónico del dueño", example = "maria.gomez@example.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    private String email;

    // Las mascotas se consultan mediante un endpoint separado.
}
