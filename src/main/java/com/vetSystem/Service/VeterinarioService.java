package com.vetSystem.Service;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.VeterinarioMapper;
import com.vetSystem.Repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor  // Lombok: genera constructor con los campos final
public class VeterinarioService implements InterfaceService<VeterinarioDTO> {

    private final VeterinarioRepository veterinarioRepository;
    private final VeterinarioMapper veterinarioMapper;

    // Registra un veterinario y valida que la matrícula no esté repetida
    @Override
    public VeterinarioDTO registrarEntidad(VeterinarioDTO dto) {
        if (veterinarioRepository.existsByMatricula(dto.getMatricula())) {
            throw new DuplicateResourceException(
                    "Ya existe un veterinario con matricula: " + dto.getMatricula());
        }

        Veterinario veterinario = veterinarioMapper.toEntity(dto);
        return veterinarioMapper.toDTO(veterinarioRepository.save(veterinario));
    }

    // Busca por ID y devuelve un Optional vacío cuando no existe
    @Override
    public Optional<VeterinarioDTO> buscarPorId(Long id) {
        Optional<Veterinario> veterinario = veterinarioRepository.findById(id);
        if (veterinario.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(veterinarioMapper.toDTO(veterinario.get()));
    }

    // Lista todos los veterinarios como DTO
    @Override
    public List<VeterinarioDTO> listarEntidades() {
        List<Veterinario> veterinarios = veterinarioRepository.findAll();
        List<VeterinarioDTO> resultado = new ArrayList<>();
        for (Veterinario veterinario : veterinarios) {
            resultado.add(veterinarioMapper.toDTO(veterinario));
        }
        return resultado;
    }

    // Modifica los datos editables de un veterinario existente
    @Override
    public VeterinarioDTO modificarEntidad(VeterinarioDTO dto) {
        Veterinario veterinario = veterinarioRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", dto.getId()));

        veterinario.setNombre(dto.getNombre());
        veterinario.setApellido(dto.getApellido());
        veterinario.setEspecialidad(dto.getEspecialidad());
        veterinario.setEmail(dto.getEmail());
        // La matrícula no se actualiza: es el identificador de negocio

        return veterinarioMapper.toDTO(veterinarioRepository.save(veterinario));
    }

    // Elimina un veterinario después de validar que exista
    @Override
    public void eliminarEntidad(Long id) {
        Veterinario veterinario = veterinarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", id));
        veterinarioRepository.delete(veterinario);
    }

    // Busca un veterinario por su matrícula
    @Override
    public Optional<VeterinarioDTO> buscarPorString(String valor) {
        Optional<Veterinario> veterinario = veterinarioRepository.findByMatricula(valor);
        if (veterinario.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(veterinarioMapper.toDTO(veterinario.get()));
    }
}
