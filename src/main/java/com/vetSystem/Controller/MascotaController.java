package com.vetSystem.Controller;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Exception.ResourceNotFoundException;
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

import java.util.Optional;

@Tag(name = "Mascotas", description = "Alta, consulta, modificación y baja de las mascotas de la clínica")
@RestController
@RequestMapping("/api/mascotas")
@RequiredArgsConstructor
public class MascotaController {

    private final MascotaService mascotaService;

    // GET /api/mascotas → lista todas las mascotas (DTO plano con duenioId/duenioNombre)
    @Operation(summary = "Listar mascotas, con búsqueda opcional por texto",
            description = "Sin el parámetro 'buscar' devuelve todas las mascotas. Con texto devuelve las "
                    + "que lo contienen en el nombre, la especie o la raza de la mascota, o en el nombre o "
                    + "apellido del dueño, sin distinguir mayúsculas. Si ninguna coincide devuelve una lista "
                    + "vacía, no un 404.")
    @ApiResponse(responseCode = "200",
            description = "Una página de mascotas: todas, o sólo las que coinciden con el texto")
    @GetMapping
    public ResponseEntity<PaginaDTO<MascotaDTO>> getAllMascotas(
            @Parameter(description = "Texto a buscar en nombre, especie o raza de la mascota, o nombre/apellido "
                    + "del dueño. Si se omite o viene vacío se devuelven todas.", example = "perro")
            @RequestParam(required = false) String buscar,
            @Parameter(description = "Número de página, empezando en 0. Si se omite, 0.", example = "0")
            @RequestParam(required = false) Integer pagina,
            @Parameter(description = "Cuántas mascotas trae cada página. Si se omite, 10. Máximo 200.",
                    example = "10")
            @RequestParam(required = false) Integer tamanio,
            @Parameter(description = "Campo por el que ordenar: id, nombre, especie, raza, "
                    + "fechaNacimiento o duenioNombre. Cualquier otro valor usa el orden por "
                    + "defecto (nombre).", example = "nombre")
            @RequestParam(required = false) String orden,
            @Parameter(description = "asc o desc. Por defecto asc.", example = "asc")
            @RequestParam(required = false) String direccion) {
        return ResponseEntity.ok(
                mascotaService.buscarPorTexto(buscar, pagina, tamanio, orden, direccion));
    }

    // GET /api/mascotas/{id} → busca una mascota por ID
    @Operation(summary = "Buscar una mascota por ID",
            description = "Devuelve los datos de la mascota con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota encontrada"),
            @ApiResponse(responseCode = "404", description = "No existe una mascota con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<MascotaDTO> getMascotaById(
            @Parameter(description = "ID de la mascota a buscar", example = "1") @PathVariable Long id) {
        Optional<MascotaDTO> mascota = mascotaService.buscarPorId(id);
        if (mascota.isEmpty()) {
            throw new ResourceNotFoundException("Mascota", id);
        }
        return ResponseEntity.ok(mascota.get());
    }

    // POST /api/mascotas → crea una mascota; el dueño viaja como duenioId en el body
    // (antes era ?duenioId= por query param — con DTOs el request queda autocontenido)
    @Operation(summary = "Registrar una nueva mascota",
            description = "Crea una mascota y la asocia al dueño indicado por duenioId.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Mascota creada"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe un dueño con el duenioId indicado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<MascotaDTO> createMascota(@Valid @RequestBody MascotaDTO dto) {
        MascotaDTO nueva = mascotaService.registrarEntidad(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(nueva);  // HTTP 201
    }

    // PUT /api/mascotas/{id} → actualiza una mascota existente
    @Operation(summary = "Modificar una mascota",
            description = "Actualiza los datos de la mascota. El dueño no se modifica.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Mascota actualizada"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe una mascota con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<MascotaDTO> updateMascota(
            @Parameter(description = "ID de la mascota a modificar", example = "1") @PathVariable Long id,
            @Valid @RequestBody MascotaDTO dto) {
        dto.setId(id);  // el id de la URL manda sobre el del body
        return ResponseEntity.ok(mascotaService.modificarEntidad(dto));
    }

    // DELETE /api/mascotas/{id} → elimina una mascota
    @Operation(summary = "Eliminar una mascota",
            description = "Borra la mascota. Si tiene turnos registrados la baja se rechaza.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Mascota eliminada, sin contenido de respuesta"),
            @ApiResponse(responseCode = "404", description = "No existe una mascota con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "La mascota tiene turnos y no se puede eliminar",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMascota(
            @Parameter(description = "ID de la mascota a eliminar", example = "1") @PathVariable Long id) {
        mascotaService.eliminarEntidad(id);
        return ResponseEntity.noContent().build();  // HTTP 204
    }
}
