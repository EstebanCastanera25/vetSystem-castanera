package com.patitas.turno.domain.exception;

// la lanza el fallback del circuit breaker cuando el otro microservicio no responde
public class ServicioNoDisponibleException extends RuntimeException {
     public ServicioNoDisponibleException(String servicio) {
        super("El servicio " + servicio + " no esta disponible en este momento, intenta nuevamente en unos minutos");
    }
    
}
