package com.vetSystem.Exception;


// Regla de negocio: no se puede recetar un medicamento que no tiene stock -> HTTP 422
public class StockInsuficienteException extends RuntimeException {
     public StockInsuficienteException(String message) {
        super(message);
    }
    
}
