package com.vetSystem.Controller;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    // GET /api/mascotas → lista todas las mascotas (DTO plano con duenioId/duenioNombre)
    @GetMapping
    public ResponseEntity<List<MascotaDTO>> getAllMascotas() {
        return ResponseEntity.ok(mascotaService.listarEntidades());
    }

    // GET /api/mascotas/{id} → busca una mascota por ID
    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> getMascotaById(@PathVariable Long id) {
        Optional<MascotaDTO> mascota = mascotaService.buscarPorId(id);
        if (mascota.isEmpty()) {
            throw new ResourceNotFoundException("Mascota", id);
        }
        return ResponseEntity.ok(mascota.get());
    }

    // POST /api/mascotas → crea una mascota; el dueño viaja como duenioId en el body
    // (antes era ?duenioId= por query param — con DTOs el request queda autocontenido)
    @PostMapping
    public ResponseEntity<MascotaDTO> createMascota(@Valid @RequestBody MascotaDTO dto) {
        MascotaDTO nueva = mascotaService.registrarEntidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);  // HTTP 201
    }

    // PUT /api/mascotas/{id} → actualiza una mascota existente
    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> updateMascota(@PathVariable Long id, @Valid @RequestBody MascotaDTO dto) {
        dto.setId(id);  // el id de la URL manda sobre el del body
        return ResponseEntity.ok(mascotaService.modificarEntidad(dto));
    }

    // DELETE /api/mascotas/{id} → elimina una mascota
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        mascotaService.eliminarEntidad(id);
        return ResponseEntity.noContent().build();  // HTTP 204
    }
}
