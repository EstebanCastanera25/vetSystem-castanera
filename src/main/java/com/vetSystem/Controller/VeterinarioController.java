package com.vetSystem.Controller;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.VeterinarioService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    // GET /api/veterinarios → lista todos los veterinarios
    @GetMapping
    public ResponseEntity<List<VeterinarioDTO>> getAllVeterinarios() {
        return ResponseEntity.ok(veterinarioService.listarEntidades());
    }

    // GET /api/veterinarios/{id} → busca un veterinario por ID
    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> getVeterinarioById(@PathVariable Long id) {
        return veterinarioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // POST /api/veterinarios → crea un veterinario
    @PostMapping
    public ResponseEntity<?> createVeterinario(@RequestBody VeterinarioDTO dto) {
        try {
            VeterinarioDTO nuevo = veterinarioService.registrarEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }

    // PUT /api/veterinarios/{id} → actualiza un veterinario
    @PutMapping("/{id}")
    public ResponseEntity<?> updateVeterinario(
            @PathVariable Long id, @RequestBody VeterinarioDTO dto) {
        try {
            dto.setId(id);
            return ResponseEntity.ok(veterinarioService.modificarEntidad(dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/veterinarios/{id} → elimina un veterinario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinario(@PathVariable Long id) {
        try {
            veterinarioService.eliminarEntidad(id);
            return ResponseEntity.noContent().build();
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
