package com.patitas.turno.adapter.in.web;

import com.patitas.turno.adapter.in.web.dto.TurnoMapper;
import com.patitas.turno.adapter.in.web.dto.TurnoRequest;
import com.patitas.turno.adapter.in.web.dto.TurnoResponse;
import com.patitas.turno.domain.port.in.TurnoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    // se inyecta la interface (puerto de entrada), no la implementacion
    private final TurnoService turnoService;

    @GetMapping
    public ResponseEntity<List<TurnoResponse>> listar() {
        return ResponseEntity.ok(TurnoMapper.toResponseList(turnoService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(TurnoMapper.toResponse(turnoService.findById(id)));
    }

    @GetMapping("/veterinario/{veterinarioId}")
    public ResponseEntity<List<TurnoResponse>> buscarPorVeterinarioId(@PathVariable Long veterinarioId) {
        return ResponseEntity.ok(TurnoMapper.toResponseList(turnoService.findByVeterinarioId(veterinarioId)));
    }

    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<TurnoResponse>> buscarPorMascotaId(@PathVariable Long mascotaId) {
        return ResponseEntity.ok(TurnoMapper.toResponseList(turnoService.findByMascotaId(mascotaId)));
    }

    @PostMapping
    public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest request) {
        TurnoResponse creado = TurnoMapper.toResponse(turnoService.save(TurnoMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<TurnoResponse> cancelar(@PathVariable Long id) {
        return ResponseEntity.ok(TurnoMapper.toResponse(turnoService.cancelar(id)));
    }
}
