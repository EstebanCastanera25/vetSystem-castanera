package com.patitas.turno.domain.exception;

public class MascotaNotFoundException extends RuntimeException {
    public MascotaNotFoundException(Long id) {
        super("No se encontro la mascota con id " + id);
    }
}
