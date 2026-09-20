package com.vetSystem.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {

    @NotNull(message = "La fecha es obligatoria")
    @FutureOrPresent(message = "La fecha del turno no puede ser en el pasado")
    private LocalDate fecha;
    @NotNull(message = "La hora es obligatoria")
    private LocalTime hora;
    private String motivo;
    @NotNull(message = "El mascotaId es obligatorio")
    @Positive(message = "El mascotaId debe ser positivo")
    private Long mascotaId;
    @NotNull(message = "El veterinarioId es obligatorio")
    @Positive(message = "El veterinarioId debe ser positivo")
    private Long veterinarioId;
}
