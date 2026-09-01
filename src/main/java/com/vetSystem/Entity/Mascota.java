package com.vetSystem.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "mascotas")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Mascota {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column
    private String nombre;
    @Column
    private String especie;
    @Column
    private String raza;
    private LocalDate fechaNacimiento;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "duenio_id", nullable = false)
    private Duenio duenio;
}
