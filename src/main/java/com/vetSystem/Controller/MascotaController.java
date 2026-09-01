package com.vetSystem.Controller;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
        return mascotaService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/mascotas → crea una mascota; el dueño viaja como duenioId en el body
    // (antes era ?duenioId= por query param — con DTOs el request queda autocontenido)
    @PostMapping
    public ResponseEntity<?> createMascota(@RequestBody MascotaDTO dto) {
        try {
            MascotaDTO nueva = mascotaService.registrarEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);  // HTTP 201
        } catch (ResourceNotFoundException e) {
            // El dueño no existe → no se puede crear la mascota
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());  // HTTP 404
        }
    }

    // PUT /api/mascotas/{id} → actualiza una mascota existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMascota(@PathVariable Long id, @RequestBody MascotaDTO dto) {
        try {
            dto.setId(id);  // el id de la URL manda sobre el del body
            return ResponseEntity.ok(mascotaService.modificarEntidad(dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/mascotas/{id} → elimina una mascota
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        try {
            mascotaService.eliminarEntidad(id);
            return ResponseEntity.noContent().build();  // HTTP 204
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
