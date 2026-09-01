package com.vetSystem.Service;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.MascotaMapper;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Repository.MascotaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MascotaService implements InterfaceService<MascotaDTO> {

    private final MascotaRepository mascotaRepository;
    private final DuenioRepository duenioRepository;
    private final MascotaMapper mascotaMapper;

    // @Transactional(readOnly) mantiene la sesión JPA abierta para que el mapper
    // pueda leer mascota.getDuenio() (relación LAZY) al armar duenioId/duenioNombre
    @Override
    @Transactional(readOnly = true)
    public List<MascotaDTO> listarEntidades() {
        return mascotaRepository.findAll().stream()
                .map(mascotaMapper::toDTO)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<MascotaDTO> buscarPorId(Long id) {
        return mascotaRepository.findById(id).map(mascotaMapper::toDTO);
    }

    // Las mascotas de un dueño (endpoint anidado) — valida que el dueño exista
    @Transactional(readOnly = true)
    public List<MascotaDTO> listarPorDuenio(Long duenioId) {
        if (!duenioRepository.existsById(duenioId)) {
            throw new ResourceNotFoundException("Duenio", duenioId);
        }
        return mascotaRepository.findByDuenioId(duenioId).stream()
                .map(mascotaMapper::toDTO)
                .toList();
    }

    // Registrar una mascota — el dueño se resuelve desde dto.duenioId y debe existir
    @Override
    @Transactional
    public MascotaDTO registrarEntidad(MascotaDTO dto) {
        Duenio duenio = duenioRepository.findById(dto.getDuenioId())
                .orElseThrow(() -> new ResourceNotFoundException("Duenio", dto.getDuenioId()));
        Mascota mascota = mascotaMapper.toEntity(dto);  // el mapper ignora duenio...
        mascota.setDuenio(duenio);                      // ...y lo seteamos acá ya validado
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    // Modificar una mascota (el id viaja dentro del DTO; no se cambia de dueño)
    @Override
    @Transactional
    public MascotaDTO modificarEntidad(MascotaDTO dto) {
        Mascota mascota = mascotaRepository.findById(dto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", dto.getId()));
        mascota.setNombre(dto.getNombre());
        mascota.setEspecie(dto.getEspecie());
        mascota.setRaza(dto.getRaza());
        mascota.setFechaNacimiento(dto.getFechaNacimiento());
        return mascotaMapper.toDTO(mascotaRepository.save(mascota));
    }

    @Override
    public void eliminarEntidad(Long id) {
        Mascota mascota = mascotaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", id));
        mascotaRepository.delete(mascota);
    }

    // Buscar por nombre (sin distinguir mayúsculas)
    @Override
    @Transactional(readOnly = true)
    public Optional<MascotaDTO> buscarPorString(String nombre) {
        return mascotaRepository.findByNombreIgnoreCase(nombre).map(mascotaMapper::toDTO);
    }
}
