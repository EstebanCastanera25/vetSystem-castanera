package com.vetSystem.Controller;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.DuenioService;
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

    // GET /api/duenios → lista todos los dueños
    @GetMapping
    public ResponseEntity<List<Duenio>> getAllDuenios() {
        return ResponseEntity.ok(duenioService.getAllDuenios());
    }

    // GET /api/duenios/{id} → busca un dueño por ID
    @GetMapping("/{id}")
    public ResponseEntity<Duenio> getDuenioById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(duenioService.getDuenioById(id));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();  // HTTP 404
        }
    }

    // POST /api/duenios → crea un nuevo dueño
    @PostMapping
    public ResponseEntity<?> createDuenio(@RequestBody Duenio duenio) {
        try {
            Duenio nuevo = duenioService.createDuenio(duenio);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
        } catch (DuplicateResourceException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());  // HTTP 409
        }
    }

    // PUT /api/duenios/{id} → actualiza un dueño existente
    @PutMapping("/{id}")
    public ResponseEntity<?> updateDuenio(@PathVariable Long id, @RequestBody Duenio duenio) {
        try {
            return ResponseEntity.ok(duenioService.updateDuenio(id, duenio));
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // DELETE /api/duenios/{id} → elimina un dueño
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(@PathVariable Long id) {
        try {
            duenioService.deleteDuenio(id);
            return ResponseEntity.noContent().build();  // HTTP 204
        } catch (ResourceNotFoundException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
