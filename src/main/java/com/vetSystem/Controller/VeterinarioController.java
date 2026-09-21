package com.vetSystem.Controller;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.DTO.PaginaDTO;
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

    // GET /api/veterinarios            → lista todos los veterinarios
    // GET /api/veterinarios?buscar=ana → sólo los que coinciden (el frontend lo llama con debounce)
    //
    // El buscador es un PARÁMETRO OPCIONAL del listado, no un endpoint /buscar aparte: es el
    // mismo recurso y la misma representación, lo único que cambia es cuántos elementos trae.
    //
    // El controller no decide nada: siempre delega. La regla "sin texto = todos" es del service.
    @Operation(summary = "Listar veterinarios, con búsqueda opcional por texto",
            description = "Sin el parámetro 'buscar' devuelve todos los veterinarios. Con texto devuelve los "
                    + "que lo contienen en el nombre, el apellido, la matrícula o la especialidad, sin distinguir "
                    + "mayúsculas. Si ninguno coincide devuelve una lista vacía, no un 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Una página de veterinarios: todos, o sólo los que coinciden con el texto"),
            @ApiResponse(responseCode = "400", description = "Algún parámetro no es un número",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PaginaDTO<VeterinarioDTO>> getAllVeterinarios(
            @Parameter(description = "Texto a buscar en nombre, apellido, matrícula o especialidad. "
                    + "Si se omite o viene vacío se devuelven todos.", example = "ana")
            @RequestParam(required = false) String buscar,
            @Parameter(description = "Número de página, empezando en 0. Si se omite, 0.",
                    example = "0")
            @RequestParam(required = false) Integer pagina,
            @Parameter(description = "Cuántos veterinarios trae cada página. Si se omite, 10. Máximo 200.",
                    example = "10")
            @RequestParam(required = false) Integer tamanio,
            @Parameter(description = "Campo por el que ordenar: id, nombre, apellido, matricula, "
                    + "especialidad o email. Cualquier otro valor usa el orden por defecto "
                    + "(apellido y nombre).", example = "apellido")
            @RequestParam(required = false) String orden,
            @Parameter(description = "asc o desc. Por defecto asc.", example = "asc")
            @RequestParam(required = false) String direccion) {
        return ResponseEntity.ok(
                veterinarioService.buscarPorTexto(buscar, pagina, tamanio, orden, direccion));
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
