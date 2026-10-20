package com.patitas.duenio.adapter.in.web;

import com.patitas.duenio.adapter.in.web.dto.DuenioMapper;
import com.patitas.duenio.adapter.in.web.dto.DuenioRequest;
import com.patitas.duenio.adapter.in.web.dto.DuenioResponse;
import com.patitas.duenio.domain.port.in.DuenioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    // se inyecta la interface (puerto de entrada), no la implementacion
    private final DuenioService duenioService;

    @GetMapping
    public ResponseEntity<List<DuenioResponse>> listar() {
        return ResponseEntity.ok(DuenioMapper.toResponseList(duenioService.findAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DuenioResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(DuenioMapper.toResponse(duenioService.findById(id)));
    }

    @PostMapping
    public ResponseEntity<DuenioResponse> crear(@Valid @RequestBody DuenioRequest request) {
        DuenioResponse creado = DuenioMapper.toResponse(duenioService.save(DuenioMapper.toEntity(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<DuenioResponse> actualizar(@PathVariable Long id, @Valid @RequestBody DuenioRequest request) {
        DuenioResponse actualizado = DuenioMapper.toResponse(duenioService.update(id, DuenioMapper.toEntity(request)));
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        duenioService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
