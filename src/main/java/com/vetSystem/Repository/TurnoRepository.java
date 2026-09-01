package com.vetSystem.Repository;

import com.vetSystem.Entity.Turno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface TurnoRepository extends JpaRepository<Turno, Long> {

    // Validar superposición: ¿el veterinario ya tiene un turno ese día a esa hora?
    // SELECT COUNT(*) > 0 FROM turnos WHERE veterinario_id = ? AND fecha = ? AND hora = ?
    boolean existsByVeterinarioIdAndFechaAndHora(Long veterinarioId, LocalDate fecha, LocalTime hora);

    // Agenda de un veterinario en una fecha
    List<Turno> findByVeterinarioIdAndFecha(Long veterinarioId, LocalDate fecha);

    // Historial de turnos de una mascota, del más reciente al más viejo
    List<Turno> findByMascotaIdOrderByFechaDescHoraDesc(Long mascotaId);
}
