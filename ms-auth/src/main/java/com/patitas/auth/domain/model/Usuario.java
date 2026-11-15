package com.patitas.auth.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true, nullable = false)
    private String username;
    // se guarda encriptada con BCrypt, nunca en texto plano
    @Column(nullable = false)
    private String password;
    // ADMIN, VET o RECEPCIONISTA
    @Column(nullable = false)
    private String rol;
}
