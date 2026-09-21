package com.vetSystem.Service;

import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.DTO.TurnoRequestDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Entity.Turno;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Exception.TurnoSuperpuestoException;
import com.vetSystem.Mapper.TurnoMapper;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.Repository.TurnoRepository;
import com.vetSystem.Repository.VeterinarioRepository;
import com.vetSystem.util.PaginaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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

    // El orden con el que se listan los turnos si el cliente no pide ninguno.
    // Termina en el id a proposito: dos turnos con la misma fecha y hora tienen que
    // quedar siempre en el mismo orden relativo entre una pagina y otra.
    private static final Sort ORDEN_POR_DEFECTO =
            Sort.by(Sort.Order.desc("fecha"), Sort.Order.desc("hora"), Sort.Order.desc("id"));

    // La clave es el nombre de la columna del frontend y el valor es el campo real.
    // mascotaNombre y veterinarioNombre viven dentro de relaciones, por eso se traducen.
    // La lista tambien protege: un campo inexistente haria fallar a Hibernate al armar
    // la consulta y la API devolveria un 500 disparable desde la URL.
    // Map.of admite hasta 10 pares; acá hay 8. Si hiciera falta un noveno campo, hay que
    // pasar a Map.ofEntries.
    private static final Map<String, String> CAMPOS_ORDENABLES = Map.of(
            "id", "id",
            "fecha", "fecha",
            "hora", "hora",
            // El estado es un enum guardado como texto: ordena alfabéticamente
            // (ATENDIDO, CANCELADO, CONFIRMADO, PENDIENTE), no por el avance del turno
            "estado", "estado",
            "motivo", "motivo",
            "observaciones", "observaciones",
            "mascotaNombre", "mascota.nombre",
            "veterinarioNombre", "veterinario.nombre");

    @Transactional(readOnly = true)
    // Con estado en null devuelve todos; con un estado, sólo los de ese estado.
    // La regla "sin filtro = todos" vive acá, igual que en el buscador de las otras
    // pantallas: el controller no decide nada.
    public PaginaDTO<TurnoResponseDTO> listarTurnos(EstadoTurno estado, Integer pagina,
                                                    Integer tamanio, String orden, String direccion) {
        Sort ordenPedido = PaginaUtil.armarOrden(orden, direccion, CAMPOS_ORDENABLES, ORDEN_POR_DEFECTO);
        Pageable pageable = PaginaUtil.armarPageable(pagina, tamanio, ordenPedido);

        // Se pagina en la BASE (LIMIT/OFFSET), no en memoria: traer todo para despues
        // quedarse con diez seria paginar la pantalla, no la consulta.
        Page<Turno> paginaDeTurnos;
        if (estado == null) {
            paginaDeTurnos = turnoRepository.findAll(pageable);
        } else {
            paginaDeTurnos = turnoRepository.findByEstado(estado, pageable);
        }

        List<TurnoResponseDTO> contenido = new ArrayList<>();
        for (Turno turno : paginaDeTurnos.getContent()) {
            contenido.add(turnoMapper.toDTO(turno));
        }
        return PaginaUtil.armar(contenido, paginaDeTurnos);
    }

    @Transactional(readOnly = true)
    public Optional<TurnoResponseDTO> buscarPorId(Long id) {
        Optional<Turno> turno = turnoRepository.findById(id);
        if (turno.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(turnoMapper.toDTO(turno.get()));
    }

    // Agenda: turnos de un veterinario en una fecha
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> buscarAgenda(Long veterinarioId, LocalDate fecha) {
        if (!veterinarioRepository.existsById(veterinarioId)) {
            throw new ResourceNotFoundException("Veterinario", veterinarioId);
        }
        List<Turno> turnos = turnoRepository.findByVeterinarioIdAndFecha(veterinarioId, fecha);
        List<TurnoResponseDTO> resultado = new ArrayList<>();
        for (Turno turno : turnos) {
            resultado.add(turnoMapper.toDTO(turno));
        }
        return resultado;
    }

    // Historial de turnos de una mascota
    @Transactional(readOnly = true)
    public List<TurnoResponseDTO> historialDeMascota(Long mascotaId) {
        if (!mascotaRepository.existsById(mascotaId)) {
            throw new ResourceNotFoundException("Mascota", mascotaId);
        }
        List<Turno> turnos = turnoRepository.findByMascotaIdOrderByFechaDescHoraDesc(mascotaId);
        List<TurnoResponseDTO> resultado = new ArrayList<>();
        for (Turno turno : turnos) {
            resultado.add(turnoMapper.toDTO(turno));
        }
        return resultado;
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
            throw new TurnoSuperpuestoException("El veterinario ya tiene un turno el "
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
