package com.patitas.auth.domain.exception;

// usuario inexistente o contrasenia incorrecta: no decimos cual de las dos para no dar pistas
public class CredencialesInvalidasException extends RuntimeException {
    public CredencialesInvalidasException() {
        super("Usuario o contrasenia incorrectos");
    }
}
