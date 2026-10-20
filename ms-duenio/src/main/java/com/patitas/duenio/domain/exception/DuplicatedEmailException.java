package com.patitas.duenio.domain.exception;

public class DuplicatedEmailException extends RuntimeException {
    public DuplicatedEmailException(String email) {
        super("Ya existe un duenio con el email " + email);
    }
}
