package com.vetSystem.Repository;

import com.vetSystem.Entity.Duenio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DuenioRepository extends JpaRepository<Duenio, Long> {

    // Query derivation: Spring genera el SQL leyendo el nombre del método
    // SELECT COUNT(*) > 0 FROM duenios WHERE cedula = ?
    boolean existsByCedula(String cedula);

    // SELECT * FROM duenios WHERE email = ?
    Optional<Duenio> findByEmail(String email);
}
