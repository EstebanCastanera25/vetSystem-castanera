package com.patitas.duenio.adapter.out.persistence;

import com.patitas.duenio.domain.model.Duenio;
import com.patitas.duenio.domain.port.out.DuenioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// Adaptador de salida: Spring Data implementa el puerto DuenioRepository
@Repository
public interface DuenioJpaRepository extends JpaRepository<Duenio, Long>, DuenioRepository {

    boolean existsByEmail(String email);
}
