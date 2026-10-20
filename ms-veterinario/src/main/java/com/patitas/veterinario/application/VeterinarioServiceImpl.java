package com.patitas.veterinario.application;

import com.patitas.veterinario.domain.exception.VeterinarioNotFoundException;
import com.patitas.veterinario.domain.model.Veterinario;
import com.patitas.veterinario.domain.port.in.VeterinarioService;
import com.patitas.veterinario.domain.port.out.VeterinarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class VeterinarioServiceImpl implements VeterinarioService {

    private final VeterinarioRepository veterinarioRepository;

    public VeterinarioServiceImpl(VeterinarioRepository veterinarioRepository) {
        this.veterinarioRepository = veterinarioRepository;
    }

    @Override
    public List<Veterinario> findAll() {
        return veterinarioRepository.findAll();
    }

    @Override
    public Veterinario findById(Long id) {
        Optional<Veterinario> veterinario = veterinarioRepository.findById(id);
        if (veterinario.isEmpty()) {
            throw new VeterinarioNotFoundException(id);
        }
        return veterinario.get();
    }

    @Override
    public List<Veterinario> findByEspecialidad(String especialidad) {
        return veterinarioRepository.findByEspecialidad(especialidad);
    }

    @Override
    @Transactional
    public Veterinario save(Veterinario veterinario) {
        return veterinarioRepository.save(veterinario);
    }

    @Override
    @Transactional
    public Veterinario update(Long id, Veterinario veterinario) {
        Veterinario existente = findById(id);
        existente.setNombre(veterinario.getNombre());
        existente.setApellido(veterinario.getApellido());
        existente.setMatricula(veterinario.getMatricula());
        existente.setEspecialidad(veterinario.getEspecialidad());
        existente.setEmail(veterinario.getEmail());
        return veterinarioRepository.save(existente);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        findById(id);
        veterinarioRepository.deleteById(id);
    }
}
