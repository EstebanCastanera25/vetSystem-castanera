package com.vetSystem.Service;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.DuenioMapper;
import com.vetSystem.Repository.DuenioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor  // Lombok: genera constructor con los campos final (constructor injection)
public class DuenioService implements InterfaceService<DuenioDTO> {

    private final DuenioRepository duenioRepository;
    private final DuenioMapper duenioMapper;

    // Listar todos los dueños como DTO (la entidad JPA no sale del service)
    @Override
    public List<DuenioDTO> listarEntidades() {
        List<Duenio> duenios = duenioRepository.findAll();
        List<DuenioDTO> resultado = new ArrayList<>();
        for (Duenio duenio : duenios) {
            resultado.add(duenioMapper.toDTO(duenio));
        }
        return resultado;
    }

    // Buscar por ID — Optional vacío si no existe (el controller decide el 404)
    @Override
    public Optional<DuenioDTO> buscarPorId(Long id) {
        Optional<Duenio> duenio = duenioRepository.findById(id);
        if (duenio.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(duenioMapper.toDTO(duenio.get()));
    }

    // Registrar un nuevo dueño — valida cédula duplicada
    @Override
    public DuenioDTO registrarEntidad(DuenioDTO dto) {
        if (duenioRepository.existsByCedula(dto.getCedula())) {
            throw new DuplicateResourceException("Ya existe un dueño con cédula: " + dto.getCedula());
        }
        Duenio duenio = duenioMapper.toEntity(dto);
        return duenioMapper.toDTO(duenioRepository.save(duenio));
    }

    // Modificar un dueño existente (el id viaja dentro del DTO)
    @Override
    public DuenioDTO modificarEntidad(DuenioDTO dto) {
        Duenio duenio = duenioRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", dto.getId()));
        duenio.setNombre(dto.getNombre());
        duenio.setApellido(dto.getApellido());
        duenio.setTelefono(dto.getTelefono());
        duenio.setEmail(dto.getEmail());
        // La cédula no se actualiza — es el identificador de negocio
        return duenioMapper.toDTO(duenioRepository.save(duenio));
    }

    // Eliminar un dueño — valida que exista antes de borrar
    @Override
    public void eliminarEntidad(Long id) {
        Duenio duenio = duenioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", id));
        duenioRepository.delete(duenio);
    }

    // Buscar por nombre
    @Override
    public Optional<DuenioDTO> buscarPorString(String nombre) {
        Optional<Duenio> duenio = duenioRepository.findByNombre(nombre);
        if (duenio.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(duenioMapper.toDTO(duenio.get()));
    }
}
