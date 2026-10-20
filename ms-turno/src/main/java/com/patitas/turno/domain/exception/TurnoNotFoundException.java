package com.patitas.turno.domain.exception;

public class TurnoNotFoundException extends RuntimeException {
    public TurnoNotFoundException(Long id) {
        super("No se encontro el turno con id " + id);
    }
}
