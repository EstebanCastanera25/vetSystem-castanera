
package com.vetSystem.Controller;

import com.vetSystem.DTO.MedicamentoRequestDTO;
import com.vetSystem.DTO.MedicamentoResponseDTO;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.MedicamentoService;
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


@Tag(name = "Medicamentos",
        description = "Alta, consulta, modificacion y baja de los medicamentos de la clinica")
@RestController
@RequestMapping("/api/medicamentos")
@RequiredArgsConstructor
public class MedicamentoController {
    private final MedicamentoService medicamentoService;

    @Operation(summary = "Listar todos los medicamentos",
            description = "Devuelve el catalogo completo con el stock actual de cada uno.")
    @ApiResponse(responseCode = "200", description = "Lista de medicamentos")
    @GetMapping
    public ResponseEntity<List<MedicamentoResponseDTO>> listar() {
        return ResponseEntity.ok(medicamentoService.listar());
    }

    @Operation(summary = "Buscar un medicamento por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un medicamento con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> buscarPorId(
            @Parameter(description = "ID del medicamento", example = "1") @PathVariable Long id) {
        Optional<MedicamentoResponseDTO> medicamento = medicamentoService.buscarPorId(id);
        if (medicamento.isEmpty()) {
            throw new ResourceNotFoundException("Medicamento", id);
        }
        return ResponseEntity.ok(medicamento.get());
    }

    @Operation(summary = "Registrar un nuevo medicamento")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Medicamento creado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o son invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un medicamento con ese nombre",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<MedicamentoResponseDTO> crear(
            @Valid @RequestBody MedicamentoRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicamentoService.registrar(dto));
    }

    @Operation(summary = "Modificar un medicamento",
            description = "Reemplaza todos los datos del medicamento, incluido el stock.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamento actualizado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o son invalidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un medicamento con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<MedicamentoResponseDTO> modificar(
            @Parameter(description = "ID del medicamento a modificar", example = "1")
            @PathVariable Long id,
            @Valid @RequestBody MedicamentoRequestDTO dto) {
        return ResponseEntity.ok(medicamentoService.modificar(id, dto));
    }

    @Operation(summary = "Eliminar un medicamento",
            description = "Borra el medicamento. Si ya fue recetado en algun turno la baja se rechaza.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Medicamento eliminado"),
            @ApiResponse(responseCode = "404", description = "No existe un medicamento con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El medicamento fue recetado y no se puede eliminar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @Parameter(description = "ID del medicamento a eliminar", example = "1")
            @PathVariable Long id) {
        medicamentoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }
}
