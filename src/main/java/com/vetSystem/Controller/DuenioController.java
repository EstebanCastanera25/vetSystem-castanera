package com.vetSystem.Controller;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.DuenioService;
import com.vetSystem.Service.MascotaService;
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

@Tag(name = "Dueños", description = "Alta, consulta, modificación y baja de los dueños de la clínica")
@RestController
@RequestMapping("/api/duenios")
@RequiredArgsConstructor
public class DuenioController {

    private final DuenioService duenioService;
    private final MascotaService mascotaService;

    // GET /api/duenios → lista todos los dueños (como DTO, sin la lista de mascotas)
    @Operation(summary = "Listar todos los dueños",
            description = "Devuelve todos los dueños registrados. Si no hay ninguno devuelve una lista vacía.")
    @ApiResponse(responseCode = "200", description = "Lista de dueños")
    @GetMapping
    public ResponseEntity<List<DuenioDTO>> getAllDuenios() {
        return ResponseEntity.ok(duenioService.listarEntidades());
    }

    // GET /api/duenios/{id} → busca un dueño por ID
    @Operation(summary = "Buscar un dueño por ID",
            description = "Devuelve los datos del dueño con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dueño encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<DuenioDTO> getDuenioById(
            @Parameter(description = "ID del dueño a buscar", example = "1") @PathVariable Long id) {
        Optional<DuenioDTO> duenio = duenioService.buscarPorId(id);
        if (duenio.isEmpty()) {
            throw new ResourceNotFoundException("Duenio", id);
        }
        return ResponseEntity.ok(duenio.get());
    }

    // POST /api/duenios → crea un nuevo dueño
    @Operation(summary = "Registrar un nuevo dueño",
            description = "Crea un dueño. La cédula es el identificador de negocio: no puede repetirse.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dueño creado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Ya existe un dueño con esa cédula",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<DuenioDTO> createDuenio(@Valid @RequestBody DuenioDTO dto) {
        DuenioDTO nuevo = duenioService.registrarEntidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
    }

    // PUT /api/duenios/{id} → actualiza un dueño existente
    @Operation(summary = "Modificar un dueño",
            description = "Actualiza nombre, apellido, teléfono y email. La cédula no se modifica, "
                    + "pero debe venir en el cuerpo porque el DTO la valida.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dueño actualizado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<DuenioDTO> updateDuenio(
            @Parameter(description = "ID del dueño a modificar", example = "1") @PathVariable Long id,
            @Valid @RequestBody DuenioDTO dto) {
        dto.setId(id);  // el id de la URL manda sobre el del body
        return ResponseEntity.ok(duenioService.modificarEntidad(dto));
    }

    // GET /api/duenios/{id}/mascotas → endpoint anidado: las mascotas de un dueño
    @Operation(summary = "Listar las mascotas de un dueño",
            description = "Devuelve las mascotas que pertenecen al dueño indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de mascotas del dueño"),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/mascotas")
    public ResponseEntity<List<MascotaDTO>> getMascotasDelDuenio(
            @Parameter(description = "ID del dueño", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(mascotaService.listarPorDuenio(id));
    }

    // DELETE /api/duenios/{id} → elimina un dueño
    @Operation(summary = "Eliminar un dueño",
            description = "Borra el dueño y, en cascada, sus mascotas. Si alguna de esas mascotas "
                    + "tiene turnos registrados la baja se rechaza.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Dueño eliminado, sin contenido de respuesta"),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El dueño tiene datos asociados y no se puede eliminar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDuenio(
            @Parameter(description = "ID del dueño a eliminar", example = "1") @PathVariable Long id) {
        duenioService.eliminarEntidad(id);
        return ResponseEntity.noContent().build();  // HTTP 204
    }
}
