package com.patitas.turno.domain.port.out;

// puerto de salida: el dominio solo necesita saber si la mascota existe
public interface MascotaServicePort {

    boolean existsById(Long id);
}
