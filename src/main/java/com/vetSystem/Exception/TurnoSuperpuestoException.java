package com.vetSystem.Exception;

// Regla de negocio: un veterinario no puede tener dos turnos en el mismo horario → HTTP 409
public class TurnoSuperpuestoException extends RuntimeException {

    public TurnoSuperpuestoException(String message) {
        super(message);
    }
}
