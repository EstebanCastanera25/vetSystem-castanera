package com.vetSystem.Exception;

// RuntimeException: no obliga a declarar throws en los métodos
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(recurso + " con id " + id + " no fue encontrado");
    }
}
