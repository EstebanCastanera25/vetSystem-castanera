package com.vetSystem.Service;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Repository.DuenioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor  // Lombok: genera constructor con los campos final (constructor injection)
public class DuenioService {

    private final DuenioRepository duenioRepository;

    // Obtener todos los dueños
    public List<Duenio> getAllDuenios() {
        return duenioRepository.findAll();
    }

    // Obtener un dueño por ID — lanza excepción si no existe
    public Duenio getDuenioById(Long id) {
        return duenioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", id));
    }

    // Crear un nuevo dueño — valida cédula duplicada
    public Duenio createDuenio(Duenio duenio) {
        if (duenioRepository.existsByCedula(duenio.getCedula())) {
            throw new DuplicateResourceException("Ya existe un dueño con cédula: " + duenio.getCedula());
        }
        return duenioRepository.save(duenio);
    }

    // Actualizar un dueño existente
    public Duenio updateDuenio(Long id, Duenio duenioActualizado) {
        Duenio duenio = getDuenioById(id);  // reutilizamos el método con validación
        duenio.setNombre(duenioActualizado.getNombre());
        duenio.setApellido(duenioActualizado.getApellido());
        duenio.setTelefono(duenioActualizado.getTelefono());
        duenio.setEmail(duenioActualizado.getEmail());
        // La cédula no se actualiza — es el identificador de negocio
        return duenioRepository.save(duenio);
    }

    // Eliminar un dueño
    public void deleteDuenio(Long id) {
        Duenio duenio = getDuenioById(id);  // valida que existe antes de borrar
        duenioRepository.delete(duenio);
    }
}
