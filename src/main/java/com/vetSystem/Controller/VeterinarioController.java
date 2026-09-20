package com.vetSystem.Controller;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.VeterinarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Tag(name = "Veterinarios", description = "Alta, consulta, modificación y baja de los veterinarios de la clínica")
@RestController
@RequestMapping("/api/veterinarios")
@RequiredArgsConstructor
public class VeterinarioController {

    private final VeterinarioService veterinarioService;

    // GET /api/veterinarios → lista todos los veterinarios
    @Operation(summary = "Listar todos los veterinarios",
            description = "Devuelve todos los veterinarios registrados. Si no hay ninguno devuelve una lista vacía.")
    @ApiResponse(responseCode = "200", description = "Lista de veterinarios")
    @GetMapping
    public ResponseEntity<List<VeterinarioDTO>> getAllVeterinarios() {
        return ResponseEntity.ok(veterinarioService.listarEntidades());
    }

    // GET /api/veterinarios/{id} → busca un veterinario por ID
    @Operation(summary = "Buscar un veterinario por ID",
            description = "Devuelve los datos del veterinario con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veterinario encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un veterinario con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> getVeterinarioById(
            @Parameter(description = "ID del veterinario a buscar", example = "1") @PathVariable Long id) {
        Optional<VeterinarioDTO> veterinario = veterinarioService.buscarPorId(id);
        if (veterinario.isEmpty()) {
            throw new ResourceNotFoundException("Veterinario", id);
        }
        return ResponseEntity.ok(veterinario.get());
    }

    // POST /api/veterinarios → crea un veterinario
    @Operation(summary = "Registrar un nuevo veterinario",
            description = "Crea un veterinario. La matrícula es el identificador de negocio: no puede repetirse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Veterinario creado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un veterinario con esa matrícula",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<VeterinarioDTO> createVeterinario(@Valid @RequestBody VeterinarioDTO dto) {
        VeterinarioDTO nuevo = veterinarioService.registrarEntidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);
    }

    // PUT /api/veterinarios/{id} → actualiza un veterinario
    @Operation(summary = "Modificar un veterinario",
            description = "Actualiza los datos del veterinario. La matrícula no se modifica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Veterinario actualizado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un veterinario con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<VeterinarioDTO> updateVeterinario(
            @Parameter(description = "ID del veterinario a modificar", example = "1") @PathVariable Long id,
            @Valid @RequestBody VeterinarioDTO dto) {
        dto.setId(id);
        return ResponseEntity.ok(veterinarioService.modificarEntidad(dto));
    }

    // DELETE /api/veterinarios/{id} → elimina un veterinario
    @Operation(summary = "Eliminar un veterinario",
            description = "Borra el veterinario y, en cascada, sus turnos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Veterinario eliminado, sin contenido de respuesta"),
            @ApiResponse(responseCode = "404", description = "No existe un veterinario con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVeterinario(
            @Parameter(description = "ID del veterinario a eliminar", example = "1") @PathVariable Long id) {
        veterinarioService.eliminarEntidad(id);
        return ResponseEntity.noContent().build();
    }
}
