package com.patitas.mascota.domain.port.out;

import com.patitas.mascota.domain.model.Mascota;

import java.util.List;
import java.util.Optional;

// Puerto de salida: lo que el dominio necesita para guardar y leer mascotas
public interface MascotaRepository {

    List<Mascota> findAll();

    Optional<Mascota> findById(Long id);

    List<Mascota> findByDuenioId(Long duenioId);

    Mascota save(Mascota mascota);

    void deleteById(Long id);
}
