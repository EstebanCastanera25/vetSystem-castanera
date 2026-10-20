package com.patitas.turno.domain.port.out;

import com.patitas.turno.domain.model.Turno;

import java.util.List;
import java.util.Optional;

// puerto de salida: lo que el dominio necesita para guardar y leer turnos
public interface TurnoRepository {

    List<Turno> findAll();

    Optional<Turno> findById(Long id);

    List<Turno> findByVeterinarioId(Long veterinarioId);

    List<Turno> findByMascotaId(Long mascotaId);

    Turno save(Turno turno);
}
