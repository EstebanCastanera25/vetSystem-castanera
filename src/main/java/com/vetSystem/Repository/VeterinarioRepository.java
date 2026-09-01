package com.vetSystem.Repository;

import com.vetSystem.Entity.Veterinario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    // Query derivation: Spring genera el SQL leyendo el nombre del método
    // SELECT COUNT(*) > 0 FROM veterinarios WHERE matricula = ?
    boolean existsByMatricula(String matricula);

    // SELECT * FROM veterinarios WHERE matricula = ?
    Optional<Veterinario> findByMatricula(String matricula);
}
