package com.vetSystem.Service;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Repository.DuenioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DuenioService {

    @Autowired
    private DuenioRepository duenioRepository;

    public Duenio registrarDuenio(Duenio duenio) {
        return duenioRepository.save(duenio);
    }

    public Optional<Duenio> buscarPorId(Long id) {
        return duenioRepository.findById(id);
    }

    public List<Duenio> listarTodos() {
        return duenioRepository.findAll();
    }

    public void eliminarDuenio(Long id) {
        duenioRepository.deleteById(id);
    }
}
