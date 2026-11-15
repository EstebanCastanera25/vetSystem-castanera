package com.patitas.auth.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequest(
        @NotBlank(message = "El usuario es obligatorio") String username,
        @NotBlank(message = "La contrasenia es obligatoria") String password,
        @NotBlank(message = "El rol es obligatorio") String rol) {
}
