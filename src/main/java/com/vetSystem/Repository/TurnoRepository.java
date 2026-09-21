package com.vetSystem.Repository;

import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Entity.Turno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    // Filtro del listado: los turnos que están en un estado.
    // En turnos NO hay buscador por texto como en las otras tres pantallas: lo que el usuario
    // reconoce de un turno (la mascota, el veterinario) vive en otras tablas, y para eso ya
    // está /agenda. Lo útil acá es filtrar por estado, y para eso alcanza la query derivation.
    // SELECT * FROM turnos WHERE estado = ?
    // OJO: el nombre NO lleva OrderBy. Un orden fijo hace que Spring le AGREGUE el orden
    // del Pageable con una coma detras: el fijo gana siempre y el orden que pide el
    // usuario queda de simple desempate. La tabla podria decir "ordenado por mascota"
    // mostrando los datos ordenados por fecha. El orden llega SIEMPRE en el Pageable.
    Page<Turno> findByEstado(EstadoTurno estado, Pageable pageable);
}
