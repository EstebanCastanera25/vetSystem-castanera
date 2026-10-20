package com.patitas.mascota.application;

import com.patitas.mascota.domain.exception.MascotaNotFoundException;
import com.patitas.mascota.domain.model.Mascota;
import com.patitas.mascota.domain.port.in.MascotaService;
import com.patitas.mascota.domain.port.out.MascotaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class MascotaServiceImpl implements MascotaService {

    private final MascotaRepository mascotaRepository;

    public MascotaServiceImpl(MascotaRepository mascotaRepository) {
        this.mascotaRepository = mascotaRepository;
    }

    @Override
    public List<Mascota> findAll() {
        return mascotaRepository.findAll();
    }

    @Override
    public Mascota findById(Long id) {
        Optional<Mascota> mascota = mascotaRepository.findById(id);
        if (mascota.isEmpty()) {
            throw new MascotaNotFoundException(id);
        }
        return mascota.get();
    }

    @Override
    public List<Mascota> findByDuenioId(Long duenioId) {
        return mascotaRepository.findByDuenioId(duenioId);
    }

    @Override
    @Transactional
    public Mascota save(Mascota mascota) {
        return mascotaRepository.save(mascota);
    }

    @Override
    @Transactional
    public Mascota update(Long id, Mascota mascota) {
        Mascota existente = findById(id);
        existente.setNombre(mascota.getNombre());
        existente.setEspecie(mascota.getEspecie());
        existente.setRaza(mascota.getRaza());
        existente.setFechaNacimiento(mascota.getFechaNacimiento());
        existente.setDuenioId(mascota.getDuenioId());
        return mascotaRepository.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        mascotaRepository.deleteById(id);
    }
}
