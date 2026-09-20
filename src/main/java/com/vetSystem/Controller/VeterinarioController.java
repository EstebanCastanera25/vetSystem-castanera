package com.vetSystem.Controller;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.VeterinarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        Optional<VeterinarioDTO> veterinario = veterinarioService.buscarPorId(id);
        if (veterinario.isEmpty()) {
            throw new ResourceNotFoundException("Veterinario", id);
        }
        return ResponseEntity.ok(veterinario.get());
    }

    // POST /api/veterinarios → crea un veterinario
    @PostMapping
    public ResponseEntity<VeterinarioDTO> createVeterinario(@Valid @RequestBody VeterinarioDTO dto) {
        VeterinarioDTO nuevo = veterinarioService.registrarEntidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /api/veterinarios/{id} → actualiza un veterinario
    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> updateVeterinario(
            @PathVariable Long id, @Valid @RequestBody VeterinarioDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(veterinarioService.modificarEntidad(dto));
    }

    // DELETE /api/veterinarios/{id} → elimina un veterinario
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinario(@PathVariable Long id) {
        veterinarioService.eliminarEntidad(id);
        return ResponseEntity.noContent().build();
    }
}
