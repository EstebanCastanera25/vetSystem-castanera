package com.patitas.veterinario.adapter.in.web;

import com.patitas.veterinario.adapter.in.web.dto.VeterinarioMapper;
import com.patitas.veterinario.adapter.in.web.dto.VeterinarioRequest;
import com.patitas.veterinario.adapter.in.web.dto.VeterinarioResponse;
import com.patitas.veterinario.domain.port.in.VeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    // se inyecta la interface (puerto de entrada), no la implementacion
    private final VeterinarioService veterinarioService;

    @GetMapping
    public ResponseEntity<List<VeterinarioResponse>> listar() {
        return ResponseEntity.ok(VeterinarioMapper.toResponseList(veterinarioService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(VeterinarioMapper.toResponse(veterinarioService.findById(id)));
    }

    @GetMapping("/especialidad/{especialidad}")
    public ResponseEntity<List<VeterinarioResponse>> buscarPorEspecialidad(@PathVariable String especialidad) {
        return ResponseEntity.ok(VeterinarioMapper.toResponseList(veterinarioService.findByEspecialidad(especialidad)));
    }

    @PostMapping
    public ResponseEntity<VeterinarioResponse> crear(@Valid @RequestBody VeterinarioRequest request) {
        VeterinarioResponse creado = VeterinarioMapper.toResponse(veterinarioService.save(VeterinarioMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody VeterinarioRequest request) {
        VeterinarioResponse actualizado = VeterinarioMapper.toResponse(veterinarioService.update(id, VeterinarioMapper.toEntity(request)));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        veterinarioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
