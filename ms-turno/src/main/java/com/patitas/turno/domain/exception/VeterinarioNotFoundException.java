package com.patitas.turno.domain.exception;

public class VeterinarioNotFoundException extends RuntimeException {
    public VeterinarioNotFoundException(Long id) {
        super("No se encontro el veterinario con id " + id);
    }
}
