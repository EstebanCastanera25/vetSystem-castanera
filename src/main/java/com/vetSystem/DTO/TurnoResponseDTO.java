package com.vetSystem.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import com.vetSystem.Entity.EstadoTurno;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseDTO {

    private Long id;
    private LocalDate fecha;
    private LocalTime hora;
    private String motivo;
    private EstadoTurno estado;
    private String observaciones;
    private Long mascotaId;
    private String mascotaNombre;
    private Long veterinarioId;
    private String veterinarioNombre;
}
