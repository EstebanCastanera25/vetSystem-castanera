package com.vetSystem.DTO;

import java.time.LocalDate;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Datos de una mascota atendida en la clínica veterinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {

    @Schema(description = "Identificador único de la mascota, generado por el servidor",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(description = "Nombre de la mascota", example = "Firulais")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @Schema(description = "Especie de la mascota", example = "Perro")
    @NotBlank(message = "La especie es obligatoria")
    private String especie;
    @Schema(description = "Raza de la mascota", example = "Mestizo")
    private String raza;
    @Schema(description = "Fecha de nacimiento de la mascota", example = "2020-05-10")
    @PastOrPresent(message = "La fecha de nacimiento no puede ser futura")
    private LocalDate fechaNacimiento;
    @Schema(description = "Identificador del dueño de la mascota", example = "1")
    @NotNull(message = "El duenioId es obligatorio")
    @Positive(message = "El duenioId debe ser positivo")
    private Long duenioId;
    @Schema(description = "Nombre de pila del dueño, agregado por el servidor para no tener que consultarlo aparte",
            example = "Maria", accessMode = Schema.AccessMode.READ_ONLY)
    private String duenioNombre;
}
