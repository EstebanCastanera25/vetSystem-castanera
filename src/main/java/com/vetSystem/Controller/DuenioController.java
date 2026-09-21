package com.vetSystem.Controller;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.DTO.PaginaDTO;
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

    // GET /api/duenios            → lista todos los dueños (como DTO, sin la lista de mascotas)
    // GET /api/duenios?buscar=ana → sólo los que coinciden (el frontend lo llama con debounce)
    //
    // El buscador es un PARÁMETRO OPCIONAL del listado, no un endpoint /buscar aparte: es el
    // mismo recurso y la misma representación, lo único que cambia es cuántos elementos trae.
    // (La agenda de turnos sí es un path propio porque sus parámetros son obligatorios y puede
    // devolver 404; este filtro es opcional y nunca falla.)
    //
    // El controller no decide nada: siempre delega. La regla "sin texto = todos" es del service.
    @Operation(summary = "Listar dueños, con búsqueda opcional por texto",
            description = "Sin el parámetro 'buscar' devuelve todos los dueños. Con texto devuelve los "
                    + "que lo contienen en el nombre, el apellido, la cédula o el email, sin distinguir "
                    + "mayúsculas. Si ninguno coincide devuelve una lista vacía, no un 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200",
                    description = "Una página de dueños: todos, o sólo los que coinciden con el texto"),
            @ApiResponse(responseCode = "400", description = "Algún parámetro no es un número",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PaginaDTO<DuenioDTO>> getAllDuenios(
            // OJO: 'buscar' tiene que seguir siendo el PRIMER parámetro. SwaggerDocsTest
            // verifica que la documentación lo muestre en la posición 0.
            @Parameter(description = "Texto a buscar en nombre, apellido, cédula o email. "
                    + "Si se omite o viene vacío se devuelven todos.", example = "ana")
            @RequestParam(required = false) String buscar,
            @Parameter(description = "Número de página, empezando en 0. Si se omite, 0.",
                    example = "0")
            @RequestParam(required = false) Integer pagina,
            @Parameter(description = "Cuántos dueños trae cada página. Si se omite, 10. Máximo 200.",
                    example = "10")
            @RequestParam(required = false) Integer tamanio,
            @Parameter(description = "Campo por el que ordenar: id, nombre, apellido, cedula, "
                    + "telefono o email. Cualquier otro valor usa el orden por defecto "
                    + "(apellido y nombre).", example = "apellido")
            @RequestParam(required = false) String orden,
            @Parameter(description = "asc o desc. Por defecto asc.", example = "asc")
            @RequestParam(required = false) String direccion) {
        return ResponseEntity.ok(
                duenioService.buscarPorTexto(buscar, pagina, tamanio, orden, direccion));
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
