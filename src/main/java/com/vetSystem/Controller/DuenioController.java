package com.vetSystem.Controller;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Service.DuenioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/duenios")
public class DuenioController {

    @Autowired
    private DuenioService duenioService;

    // Endpoint de prueba del Sprint 1: verifica que toda la cadena
    // Controller -> Service -> Repository -> MySQL responde
    @GetMapping
    public ResponseEntity<List<Duenio>> listarTodos() {
        return ResponseEntity.ok(duenioService.listarTodos());
    }
}
