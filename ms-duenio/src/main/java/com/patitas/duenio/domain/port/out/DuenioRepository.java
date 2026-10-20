package com.patitas.duenio.domain.port.out;

import com.patitas.duenio.domain.model.Duenio;

import java.util.List;
import java.util.Optional;

// Puerto de salida: lo que el dominio necesita para guardar y leer duenios
public interface DuenioRepository {

    List<Duenio> findAll();

    Optional<Duenio> findById(Long id);

    Duenio save(Duenio duenio);

    void deleteById(Long id);

    boolean existsByEmail(String email);
}
