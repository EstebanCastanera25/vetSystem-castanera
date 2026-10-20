package com.patitas.duenio.domain.exception;

public class DuenioNotFoundException extends RuntimeException {
    public DuenioNotFoundException(Long id) {
        super("No se encontro el duenio con id " + id);
    }
}
