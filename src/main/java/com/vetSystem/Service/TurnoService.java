package com.vetSystem.Service;

import com.vetSystem.DTO.TurnoRequestDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Entity.Turno;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.TurnoMapper;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.Repository.TurnoRepository;
import com.vetSystem.Repository.VeterinarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

// No implementa InterfaceService: el DTO de entrada (TurnoRequestDTO) y el de
// salida (TurnoResponseDTO) son distintos, así que el contrato genérico <T> no aplica.
@Service
@RequiredArgsConstructor
public class TurnoService {

    private final TurnoRepository turnoRepository;
    private final MascotaRepository mascotaRepository;
    private final VeterinarioRepository veterinarioRepository;
    private final TurnoMapper turnoMapper;

    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> listarTurnos() {
        return turnoRepository.findAll().stream()
                .map(turnoMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<TurnoResponseDTO> buscarPorId(Long id) {
        return turnoRepository.findById(id).map(turnoMapper::toDTO);
    }

    // Agenda: turnos de un veterinario en una fecha
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> buscarAgenda(Long veterinarioId, LocalDate fecha) {
        if (!veterinarioRepository.existsById(veterinarioId)) {
            throw new ResourceNotFoundException("Veterinario", veterinarioId);
        }
        return turnoRepository.findByVeterinarioIdAndFecha(veterinarioId, fecha).stream()
                .map(turnoMapper::toDTO)
                .toList();
    }

    // Historial de turnos de una mascota
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> historialDeMascota(Long mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new ResourceNotFoundException("Mascota", mascotaId);
        }
        return turnoRepository.findByMascotaIdOrderByFechaDescHoraDesc(mascotaId).stream()
                .map(turnoMapper::toDTO)
                .toList();
    }

    @Transactional
    public TurnoResponseDTO crearTurno(TurnoRequestDTO request) {
        // 1. La mascota debe existir
        Mascota mascota = mascotaRepository.findById(request.getMascotaId())
                .orElseThrow(() -> new ResourceNotFoundException("Mascota", request.getMascotaId()));

        // 2. El veterinario debe existir
        Veterinario veterinario = veterinarioRepository.findById(request.getVeterinarioId())
                .orElseThrow(() -> new ResourceNotFoundException("Veterinario", request.getVeterinarioId()));

        // 3. Regla de negocio: un veterinario no puede tener dos turnos a la misma hora
        if (turnoRepository.existsByVeterinarioIdAndFechaAndHora(
                request.getVeterinarioId(), request.getFecha(), request.getHora())) {
            throw new DuplicateResourceException("El veterinario ya tiene un turno el "
                    + request.getFecha() + " a las " + request.getHora());
        }

        // 4. Armar la entidad desde el request — el estado inicial siempre es PENDIENTE
        Turno turno = new Turno();
        turno.setFecha(request.getFecha());
        turno.setHora(request.getHora());
        turno.setMotivo(request.getMotivo());
        turno.setEstado(EstadoTurno.PENDIENTE);
        turno.setMascota(mascota);
        turno.setVeterinario(veterinario);

        return turnoMapper.toDTO(turnoRepository.save(turno));
    }

    // Actualización parcial: solo el estado (y observaciones opcionales)
    @Transactional
    public TurnoResponseDTO actualizarEstado(Long id, EstadoTurno nuevoEstado, String observaciones) {
        Turno turno = turnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Turno", id));
        turno.setEstado(nuevoEstado);
        if (observaciones != null) {
            turno.setObservaciones(observaciones);
        }
        return turnoMapper.toDTO(turnoRepository.save(turno));
    }
}
