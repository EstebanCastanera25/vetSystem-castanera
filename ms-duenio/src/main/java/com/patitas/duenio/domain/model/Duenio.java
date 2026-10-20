package com.patitas.duenio.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "duenios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Duenio {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String nombre;
    @Column(nullable = false)
    private String apellido;
    @Column(nullable = false, unique = true)
    private String cedula;
    @Column(nullable = false)
    private Integer telefono;
    @Column(nullable = false, unique = true)
    private String email;
}
