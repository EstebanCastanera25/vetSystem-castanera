package com.patitas.turno.domain.port.in;

import com.patitas.turno.domain.model.Turno;

import java.util.List;

// puerto de entrada: lo que se puede hacer con los turnos (el que, no el como)
public interface TurnoService {

    List<Turno> findAll();

    Turno findById(Long id);

    List<Turno> findByVeterinarioId(Long veterinarioId);

    List<Turno> findByMascotaId(Long mascotaId);

    Turno save(Turno turno);

    Turno cancelar(Long id);
}
