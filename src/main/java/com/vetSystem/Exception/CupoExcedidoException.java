package com.vetSystem.Exception;

// Regla de negocio: un duenio no puede superar el cupo de mascotas activas  HTTP 422
public class CupoExcedidoException  extends RuntimeException {
    public CupoExcedidoException(String message) {
        super(message);
    }
    
}
