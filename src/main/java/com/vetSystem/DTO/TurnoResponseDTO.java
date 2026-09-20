package com.vetSystem.DTO;

import java.time.LocalDate;
import java.time.LocalTime;

import com.vetSystem.Entity.EstadoTurno;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Información completa de un turno veterinario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TurnoResponseDTO {

    @Schema(description = "Identificador único del turno, generado por el servidor",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;
    @Schema(description = "Fecha programada del turno", example = "2030-03-15",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalDate fecha;
    @Schema(description = "Hora programada del turno", example = "10:30:00", type = "string",
            accessMode = Schema.AccessMode.READ_ONLY)
    private LocalTime hora;
    @Schema(description = "Motivo de la consulta veterinaria", example = "Control anual y vacunacion",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String motivo;
    @Schema(description = "Estado actual del turno", example = "PENDIENTE",
            accessMode = Schema.AccessMode.READ_ONLY)
    private EstadoTurno estado;
    @Schema(description = "Observaciones registradas durante la atención",
            example = "El paciente respondio bien al tratamiento",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String observaciones;
    @Schema(description = "Identificador de la mascota atendida", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long mascotaId;
    @Schema(description = "Nombre de la mascota atendida", example = "Firulais",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String mascotaNombre;
    @Schema(description = "Identificador del veterinario asignado", example = "1",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Long veterinarioId;
    @Schema(description = "Nombre de pila del veterinario asignado", example = "Leandro",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String veterinarioNombre;
}
