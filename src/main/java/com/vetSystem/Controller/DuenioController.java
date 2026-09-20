package com.vetSystem.Controller;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

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
        Optional<DuenioDTO> duenio = duenioService.buscarPorId(id);
        if (duenio.isEmpty()) {
            throw new ResourceNotFoundException("Duenio", id);
        }
        return ResponseEntity.ok(duenio.get());
    }

    // POST /api/duenios → crea un nuevo dueño
    @PostMapping
    public ResponseEntity<DuenioDTO> createDuenio(@Valid @RequestBody DuenioDTO dto) {
        DuenioDTO nuevo = duenioService.registrarEntidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
    }

    // PUT /api/duenios/{id} → actualiza un dueño existente
    @PutMapping("/{id}")
    public ResponseEntity<DuenioDTO> updateDuenio(@PathVariable Long id, @Valid @RequestBody DuenioDTO dto) {
        dto.setId(id);  // el id de la URL manda sobre el del body
        return ResponseEntity.ok(duenioService.modificarEntidad(dto));
    }

    // GET /api/duenios/{id}/mascotas → endpoint anidado: las mascotas de un dueño
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<List<MascotaDTO>> getMascotasDelDuenio(@PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.listarPorDuenio(id));
    }

    // DELETE /api/duenios/{id} → elimina un dueño
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(@PathVariable Long id) {
        duenioService.eliminarEntidad(id);
        return ResponseEntity.noContent().build();  // HTTP 204
    }
}
