package com.vetSystem.Service;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MascotaService {

    private final MascotaRepository mascotaRepository;
    private final DuenioRepository duenioRepository;

    public List<Mascota> getAllMascotas() {
        return mascotaRepository.findAll();
    }

    public Mascota getMascotaById(Long id) {
        return mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
    }

    // @Transactional mantiene la sesión JPA abierta para acceder a colecciones LAZY
    @Transactional
    public List<Mascota> getMascotasByDuenio(Long duenioId) {
        // Primero validamos que el dueño existe
        if (!duenioRepository.existsById(duenioId)) {
            throw new ResourceNotFoundException("Duenio", duenioId);
        }
        return mascotaRepository.findByDuenioId(duenioId);
    }

    @Transactional
    public Mascota createMascota(Long duenioId, Mascota mascota) {
        // Validar que el dueño existe antes de crear la mascota
        Duenio duenio = duenioRepository.findById(duenioId)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", duenioId));
        mascota.setDuenio(duenio);
        return mascotaRepository.save(mascota);
    }

    @Transactional
    public Mascota updateMascota(Long id, Mascota datos) {
        Mascota mascota = getMascotaById(id);
        mascota.setNombre(datos.getNombre());
        mascota.setEspecie(datos.getEspecie());
        mascota.setRaza(datos.getRaza());
        mascota.setFecha(datos.getFecha());
        return mascotaRepository.save(mascota);
    }

    public void deleteMascota(Long id) {
        Mascota mascota = getMascotaById(id);
        mascotaRepository.delete(mascota);
    }
}
