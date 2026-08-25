package com.vetSystem.Controller;

import com.vetSystem.Entity.Mascota;
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

    // GET /api/mascotas → lista todas las mascotas
    @GetMapping
    public ResponseEntity<List<Mascota>> getAllMascotas() {
        return ResponseEntity.ok(mascotaService.getAllMascotas());
    }

    // GET /api/mascotas/{id} → busca una mascota por ID
    @GetMapping("/{id}")
    public ResponseEntity<Mascota> getMascotaById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(mascotaService.getMascotaById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // POST /api/mascotas?duenioId=1 → crea una mascota para ese dueño
    @PostMapping
    public ResponseEntity<?> createMascota(@RequestParam Long duenioId,
                                           @RequestBody Mascota mascota) {
        try {
            Mascota nueva = mascotaService.createMascota(duenioId, mascota);
            return ResponseEntity.status(HttpStatus.CREATED).body(nueva);  // HTTP 201
        } catch (ResourceNotFoundException e) {
            // El dueño no existe → no se puede crear la mascota
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());  // HTTP 404
        }
    }

    // PUT /api/mascotas/{id} → actualiza una mascota existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMascota(@PathVariable Long id,
                                           @RequestBody Mascota mascota) {
        try {
            return ResponseEntity.ok(mascotaService.updateMascota(id, mascota));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/mascotas/{id} → elimina una mascota
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(@PathVariable Long id) {
        try {
            mascotaService.deleteMascota(id);
            return ResponseEntity.noContent().build();  // HTTP 204
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
