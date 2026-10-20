package com.patitas.mascota.domain.port.in;

import com.patitas.mascota.domain.model.Mascota;

import java.util.List;

// Puerto de entrada: lo que se puede hacer con las mascotas (el QUE, no el COMO)
public interface MascotaService {

    List<Mascota> findAll();

    Mascota findById(Long id);

    List<Mascota> findByDuenioId(Long duenioId);

    Mascota save(Mascota mascota);

    Mascota update(Long id, Mascota mascota);

    void deleteById(Long id);
}
