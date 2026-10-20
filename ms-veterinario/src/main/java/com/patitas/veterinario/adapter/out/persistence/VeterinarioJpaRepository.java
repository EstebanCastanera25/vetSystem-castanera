package com.patitas.veterinario.adapter.out.persistence;

import com.patitas.veterinario.domain.model.Veterinario;
import com.patitas.veterinario.domain.port.out.VeterinarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// adaptador de salida: spring data implementa el puerto VeterinarioRepository
@Repository
public interface VeterinarioJpaRepository extends JpaRepository<Veterinario, Long>, VeterinarioRepository {

    List<Veterinario> findByEspecialidad(String especialidad);
}
