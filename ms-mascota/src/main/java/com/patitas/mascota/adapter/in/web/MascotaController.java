package com.patitas.mascota.adapter.in.web;

import com.patitas.mascota.adapter.in.web.dto.MascotaMapper;
import com.patitas.mascota.adapter.in.web.dto.MascotaRequest;
import com.patitas.mascota.adapter.in.web.dto.MascotaResponse;
import com.patitas.mascota.domain.port.in.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    // se inyecta la interface (puerto de entrada), no la implementacion
    private final MascotaService mascotaService;

    @GetMapping
    public ResponseEntity<List<MascotaResponse>> listar() {
        return ResponseEntity.ok(MascotaMapper.toResponseList(mascotaService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MascotaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(MascotaMapper.toResponse(mascotaService.findById(id)));
    }

    @GetMapping("/duenio/{duenioId}")
    public ResponseEntity<List<MascotaResponse>> buscarPorDuenioId(@PathVariable Long duenioId) {
        return ResponseEntity.ok(MascotaMapper.toResponseList(mascotaService.findByDuenioId(duenioId)));
    }

    @PostMapping
    public ResponseEntity<MascotaResponse> crear(@Valid @RequestBody MascotaRequest request) {
        MascotaResponse creado = MascotaMapper.toResponse(mascotaService.save(MascotaMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<MascotaResponse> actualizar(@PathVariable Long id, @Valid @RequestBody MascotaRequest request) {
        MascotaResponse actualizado = MascotaMapper.toResponse(mascotaService.update(id, MascotaMapper.toEntity(request)));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        mascotaService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
