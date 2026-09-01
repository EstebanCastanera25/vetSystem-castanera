package com.vetSystem.Controller;

import com.vetSystem.DTO.TurnoRequestDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.TurnoService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

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
        return turnoService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // GET /api/turnos/agenda?veterinarioId=1&fecha=2026-07-10 → agenda del veterinario
    @GetMapping("/agenda")
    public ResponseEntity<?> buscarAgenda(
            @RequestParam Long veterinarioId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        try {
            return ResponseEntity.ok(turnoService.buscarAgenda(veterinarioId, fecha));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // GET /api/turnos/mascota/{mascotaId} → historial de turnos de una mascota
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<?> historialDeMascota(@PathVariable Long mascotaId) {
        try {
            return ResponseEntity.ok(turnoService.historialDeMascota(mascotaId));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    // POST /api/turnos → crea un turno a partir del TurnoRequestDTO
    @PostMapping
    public ResponseEntity<?> crearTurno(@RequestBody TurnoRequestDTO request) {
        try {
            TurnoResponseDTO nuevo = turnoService.crearTurno(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
        } catch (ResourceNotFoundException e) {
            // Mascota o veterinario inexistentes
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());  // HTTP 404
        } catch (DuplicateResourceException e) {
            // Superposición de horario
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());  // HTTP 409
        }
    }

    // PATCH /api/turnos/{id}/estado?estado=CONFIRMADO&observaciones=... → actualización parcial
    @PatchMapping("/{id}/estado")
    public ResponseEntity<?> actualizarEstado(@PathVariable Long id,
                                              @RequestParam EstadoTurno estado,
                                              @RequestParam(required = false) String observaciones) {
        try {
            return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
