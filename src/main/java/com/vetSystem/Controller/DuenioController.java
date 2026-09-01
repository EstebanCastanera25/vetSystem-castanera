package com.vetSystem.Controller;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    private final DuenioService duenioService;
    private final MascotaService mascotaService;

    // GET /api/duenios → lista todos los dueños (como DTO, sin la lista de mascotas)
    @GetMapping
    public ResponseEntity<List<DuenioDTO>> getAllDuenios() {
        return ResponseEntity.ok(duenioService.listarEntidades());
    }

    // GET /api/duenios/{id} → busca un dueño por ID
    @GetMapping("/{id}")
    public ResponseEntity<DuenioDTO> getDuenioById(@PathVariable Long id) {
        return duenioService.buscarPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());  // HTTP 404
    }

    // POST /api/duenios → crea un nuevo dueño
    @PostMapping
    public ResponseEntity<?> createDuenio(@RequestBody DuenioDTO dto) {
        try {
            DuenioDTO nuevo = duenioService.registrarEntidad(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());  // HTTP 409
        }
    }

    // PUT /api/duenios/{id} → actualiza un dueño existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDuenio(@PathVariable Long id, @RequestBody DuenioDTO dto) {
        try {
            dto.setId(id);  // el id de la URL manda sobre el del body
            return ResponseEntity.ok(duenioService.modificarEntidad(dto));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // GET /api/duenios/{id}/mascotas → endpoint anidado: las mascotas de un dueño
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<?> getMascotasDelDuenio(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mascotaService.listarPorDuenio(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/duenios/{id} → elimina un dueño
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(@PathVariable Long id) {
        try {
            duenioService.eliminarEntidad(id);
            return ResponseEntity.noContent().build();  // HTTP 204
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
