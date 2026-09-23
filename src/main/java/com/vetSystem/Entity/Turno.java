package com.vetSystem.Entity;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "turnos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Turno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private LocalDate fecha;
    @Column(nullable = false)
    private LocalTime hora;
    @Column
    private String motivo;
    @Column
    private String observaciones;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoTurno estado;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mascota_id", nullable = false)
    private Mascota mascota;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "veterinario_id", nullable = false)
    private Veterinario veterinario;


    @JoinTable(
            name = "turno_medicamento",
            joinColumns = @JoinColumn(name = "turno_id"),
            inverseJoinColumns = @JoinColumn(name = "medicamento_id"))
    @ManyToMany(fetch = FetchType.LAZY)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private List<Medicamento> medicamentos = new ArrayList<>();
}
