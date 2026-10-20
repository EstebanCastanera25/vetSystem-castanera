package com.patitas.duenio.application;

import com.patitas.duenio.domain.exception.DuenioNotFoundException;
import com.patitas.duenio.domain.exception.DuplicatedEmailException;
import com.patitas.duenio.domain.model.Duenio;
import com.patitas.duenio.domain.port.in.DuenioService;
import com.patitas.duenio.domain.port.out.DuenioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class DuenioServiceImpl implements DuenioService {

    private final DuenioRepository duenioRepository;

    public DuenioServiceImpl(DuenioRepository duenioRepository) {
        this.duenioRepository = duenioRepository;
    }

    @Override
    public List<Duenio> findAll() {
        return duenioRepository.findAll();
    }

    @Override
    public Duenio findById(Long id) {
        Optional<Duenio> duenio = duenioRepository.findById(id);
        if (duenio.isEmpty()) {
            throw new DuenioNotFoundException(id);
        }
        return duenio.get();
    }

    @Override
    @Transactional
    public Duenio save(Duenio duenio) {
        if (duenioRepository.existsByEmail(duenio.getEmail())) {
            throw new DuplicatedEmailException(duenio.getEmail());
        }
        return duenioRepository.save(duenio);
    }

    @Override
    @Transactional
    public Duenio update(Long id, Duenio duenio) {
        Duenio existente = findById(id);
        // si cambia el email, el nuevo no puede estar usado por otro duenio
        if (!existente.getEmail().equals(duenio.getEmail()) && duenioRepository.existsByEmail(duenio.getEmail())) {
            throw new DuplicatedEmailException(duenio.getEmail());
        }
        existente.setNombre(duenio.getNombre());
        existente.setApellido(duenio.getApellido());
        existente.setCedula(duenio.getCedula());
        existente.setTelefono(duenio.getTelefono());
        existente.setEmail(duenio.getEmail());
        return duenioRepository.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        duenioRepository.deleteById(id);
    }
}
