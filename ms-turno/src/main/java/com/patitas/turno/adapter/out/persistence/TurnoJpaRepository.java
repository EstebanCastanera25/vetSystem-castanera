package com.patitas.turno.adapter.out.persistence;

import com.patitas.turno.domain.model.Turno;
import com.patitas.turno.domain.port.out.TurnoRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// adaptador de salida: spring data implementa el puerto TurnoRepository
@Repository
public interface TurnoJpaRepository extends JpaRepository<Turno, Long>, TurnoRepository {

    List<Turno> findByVeterinarioId(Long veterinarioId);

    List<Turno> findByMascotaId(Long mascotaId);
}
