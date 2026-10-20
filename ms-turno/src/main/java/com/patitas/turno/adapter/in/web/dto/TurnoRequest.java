package com.patitas.turno.adapter.in.web.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequest {

    @NotNull(message = "La fecha y hora es obligatoria")
    @Future(message = "La fecha y hora debe ser futura")
    private LocalDateTime fechaHora;
    @NotNull(message = "La mascota es obligatoria")
    private Long mascotaId;
    @NotNull(message = "El veterinario es obligatorio")
    private Long veterinarioId;
    @NotBlank(message = "El motivo es obligatorio")
    private String motivo;
}
