package com.patitas.mascota.adapter.out.persistence;

import com.patitas.mascota.domain.model.Mascota;
import com.patitas.mascota.domain.port.out.MascotaRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

// Adaptador de salida: Spring Data implementa el puerto MascotaRepository
@Repository
public interface MascotaJpaRepository extends JpaRepository<Mascota, Long>, MascotaRepository {

    List<Mascota> findByDuenioId(Long duenioId);
}
