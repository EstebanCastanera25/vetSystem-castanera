package com.vetSystem.Repository;

import com.vetSystem.Entity.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // Todas las mascotas de un dueño específico
    // SELECT * FROM mascotas WHERE duenio_id = ?
    List<Mascota> findByDuenioId(Long duenioId);

    // Verificar si existe una mascota con ese nombre para ese dueño
    boolean existsByNombreAndDuenioId(String nombre, Long duenioId);

    // Contar mascotas por especie (útil para reportes futuros)
    long countByEspecie(String especie);

    // Usado por buscarPorString del contrato InterfaceService
    Optional<Mascota> findByNombreIgnoreCase(String nombre);
}
