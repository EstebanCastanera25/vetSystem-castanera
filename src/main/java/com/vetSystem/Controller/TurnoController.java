package com.vetSystem.Controller;

import com.vetSystem.DTO.TurnoRequestDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    // GET /api/turnos → lista todos los turnos (como TurnoResponseDTO)
    @GetMapping
    public ResponseEntity<List<TurnoResponseDTO>> listarTurnos() {
        return ResponseEntity.ok(turnoService.listarTurnos());
    }

    // GET /api/turnos/{id} → busca un turno por ID
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> buscarPorId(@PathVariable Long id) {
        Optional<TurnoResponseDTO> turno = turnoService.buscarPorId(id);
        if (turno.isEmpty()) {
            throw new ResourceNotFoundException("Turno", id);
        }
        return ResponseEntity.ok(turno.get());
    }

    // GET /api/turnos/agenda?veterinarioId=1&fecha=2026-07-10 → agenda del veterinario
    @GetMapping("/agenda")
    public ResponseEntity<List<TurnoResponseDTO>> buscarAgenda(
            @RequestParam Long veterinarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.buscarAgenda(veterinarioId, fecha));
    }

    // GET /api/turnos/mascota/{mascotaId} → historial de turnos de una mascota
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<TurnoResponseDTO>> historialDeMascota(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(turnoService.historialDeMascota(mascotaId));
    }

    // POST /api/turnos → crea un turno a partir del TurnoRequestDTO
    @PostMapping
    public ResponseEntity<TurnoResponseDTO> crearTurno(@Valid @RequestBody TurnoRequestDTO request) {
        TurnoResponseDTO nuevo = turnoService.crearTurno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
    }

    // PATCH /api/turnos/{id}/estado?estado=CONFIRMADO&observaciones=... → actualización parcial
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponseDTO> actualizarEstado(@PathVariable Long id,
                                                             @RequestParam EstadoTurno estado,
                                                             @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
    }
}
