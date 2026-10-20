package com.patitas.turno.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "turnos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDateTime fechaHora;
    @Column(nullable = false)
    private Long mascotaId;
    @Column(nullable = false)
    private Long veterinarioId;
    @Column(nullable = false)
    private String motivo;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;
}
