package com.vetSystem.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoRequestDTO {

    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private Long mascotaId;
    private Long veterinarioId;
}
