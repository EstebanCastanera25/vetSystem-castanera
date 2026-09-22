package com.vetSystem.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Datos de un veterinario de la clínica veterinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioDTO {

    @Schema(description = "Identificador único del veterinario, generado por el servidor",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(description = "Nombre de pila del veterinario", example = "Leandro")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @Schema(description = "Apellido del veterinario", example = "Perez")
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @Schema(description = "Matrícula profesional del veterinario", example = "MV-4521")
    @NotBlank(message = "La matrícula es obligatoria")
    // CASE_INSENSITIVE a propósito: el usuario puede escribir "mv-4521" y el service la
    // guarda como "MV-4521". Bean Validation corre en el controller, ANTES del service,
    // así que si el patrón exigiera mayúsculas el pedido moriría con un 400 y nunca
    // llegaría al lugar donde se normaliza. La validación dice QUÉ FORMA tiene que tener
    // el dato; cómo se guarda lo decide el service.
    @Pattern(regexp = "MV-\\d+", flags = Pattern.Flag.CASE_INSENSITIVE,
            message = "La matrícula debe tener el formato MV-XXXX")
    private String matricula;
    @Schema(description = "Especialidad profesional del veterinario", example = "Clinica General")
    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
    @Schema(description = "Correo electrónico del veterinario",
            example = "leandro.perez@patitasfelices.com")
    @Email(message = "El email debe tener un formato válido")
    private String email;
}
