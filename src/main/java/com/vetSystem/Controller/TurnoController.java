package com.vetSystem.Controller;

import com.vetSystem.DTO.MedicamentoResponseDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.DTO.TurnoRequestDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Exception.ErrorResponse;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Service.TurnoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Tag(name = "Turnos", description = "Alta, consulta y actualización de los turnos de la clínica")
@RestController
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
public class TurnoController {

    private final TurnoService turnoService;

    // GET /api/turnos                   → lista todos los turnos (como TurnoResponseDTO)
    // GET /api/turnos?estado=PENDIENTE  → sólo los que están en ese estado
    //
    // Mismo criterio que el buscador de las otras pantallas: es un filtro OPCIONAL sobre la
    // colección, así que va como query param del listado y no como un endpoint aparte.
    // Spring convierte solo el texto de la URL al enum EstadoTurno.
    @Operation(summary = "Listar turnos, con filtro opcional por estado",
            description = "Sin el parámetro 'estado' devuelve todos los turnos. Con un estado devuelve "
                    + "sólo los de ese estado. Si no hay ninguno devuelve "
                    + "una lista vacía, no un 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de turnos: todos, o sólo los del estado pedido"),
            @ApiResponse(responseCode = "400", description = "El estado indicado no es uno de los válidos",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<PaginaDTO<TurnoResponseDTO>> listarTurnos(
            @Parameter(description = "Estado por el que filtrar. Si se omite se devuelven todos.",
                    example = "PENDIENTE")
            @RequestParam(required = false) EstadoTurno estado,
            @Parameter(description = "Numero de pagina, empezando en 0. Si se omite, 0.",
                    example = "0")
            @RequestParam(required = false) Integer pagina,
            @Parameter(description = "Cuantos turnos trae cada pagina. Si se omite, 10. Maximo 200.",
                    example = "10")
            @RequestParam(required = false) Integer tamanio,
            @Parameter(description = "Campo por el que ordenar: id, fecha, hora, estado, motivo, "
                    + "observaciones, mascotaNombre o veterinarioNombre. Cualquier otro valor usa "
                    + "el orden por defecto (fecha y hora, del más reciente al más viejo).",
                    example = "fecha")
            @RequestParam(required = false) String orden,
            @Parameter(description = "asc o desc. Por defecto asc.", example = "desc")
            @RequestParam(required = false) String direccion) {
        return ResponseEntity.ok(
                turnoService.listarTurnos(estado, pagina, tamanio, orden, direccion));
    }

    // GET /api/turnos/{id} → busca un turno por ID
    @Operation(summary = "Buscar un turno por ID",
            description = "Devuelve los datos del turno con el ID indicado.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Turno encontrado"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TurnoResponseDTO> buscarPorId(
            @Parameter(description = "ID del turno a buscar", example = "1") @PathVariable Long id) {
        Optional<TurnoResponseDTO> turno = turnoService.buscarPorId(id);
        if (turno.isEmpty()) {
            throw new ResourceNotFoundException("Turno", id);
        }
        return ResponseEntity.ok(turno.get());
    }

    // GET /api/turnos/agenda?veterinarioId=1&fecha=2026-07-10 → agenda del veterinario
    @Operation(summary = "Consultar la agenda de un veterinario",
            description = "Devuelve los turnos de un veterinario en la fecha indicada.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agenda del veterinario"),
            @ApiResponse(responseCode = "404", description = "No existe un veterinario con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/agenda")
    public ResponseEntity<List<TurnoResponseDTO>> buscarAgenda(
            @Parameter(description = "ID del veterinario", example = "1") @RequestParam Long veterinarioId,
            @Parameter(description = "Fecha de la agenda", example = "2030-03-15")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(turnoService.buscarAgenda(veterinarioId, fecha));
    }

    // GET /api/turnos/mascota/{mascotaId} → historial de turnos de una mascota
    @Operation(summary = "Consultar el historial de una mascota",
            description = "Devuelve los turnos de la mascota, del más reciente al más antiguo.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Historial de turnos de la mascota"),
            @ApiResponse(responseCode = "404", description = "No existe una mascota con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/mascota/{mascotaId}")
    public ResponseEntity<List<TurnoResponseDTO>> historialDeMascota(
            @Parameter(description = "ID de la mascota", example = "1") @PathVariable Long mascotaId) {
        return ResponseEntity.ok(turnoService.historialDeMascota(mascotaId));
    }

    // POST /api/turnos → crea un turno a partir del TurnoRequestDTO
    @Operation(summary = "Registrar un nuevo turno",
            description = "Crea un turno para una mascota con un veterinario. El veterinario no puede tener "
                    + "otro turno en la misma fecha y hora.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Turno creado"),
            @ApiResponse(responseCode = "400", description = "Faltan datos obligatorios o tienen un formato inválido",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "No existe la mascota o el veterinario indicado",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "El veterinario ya tiene un turno en esa fecha y hora",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<TurnoResponseDTO> crearTurno(@Valid @RequestBody TurnoRequestDTO request) {
        TurnoResponseDTO nuevo = turnoService.crearTurno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(nuevo);  // HTTP 201
    }

    // PATCH /api/turnos/{id}/estado?estado=CONFIRMADO&observaciones=... → actualización parcial
    @Operation(summary = "Actualizar el estado de un turno",
            description = "Cambia el estado del turno y, opcionalmente, agrega observaciones.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Estado del turno actualizado"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PatchMapping("/{id}/estado")
    public ResponseEntity<TurnoResponseDTO> actualizarEstado(
            @Parameter(description = "ID del turno a actualizar", example = "1") @PathVariable Long id,
            @Parameter(description = "Nuevo estado del turno", example = "CONFIRMADO")
            @RequestParam EstadoTurno estado,
            @Parameter(description = "Observaciones del turno",
                    example = "El paciente respondio bien al tratamiento")
            @RequestParam(required = false) String observaciones) {
        return ResponseEntity.ok(turnoService.actualizarEstado(id, estado, observaciones));
    }


    // GET /api/turnos/{id}/medicamentos -> los medicamentos recetados en ese turno
    @Operation(summary = "Listar los medicamentos recetados en un turno",
            description = "Devuelve los medicamentos que se recetaron en el turno indicado. "
                    + "Si no se receto ninguno devuelve una lista vacia, no un 404.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Medicamentos recetados en el turno"),
            @ApiResponse(responseCode = "404", description = "No existe un turno con ese ID",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}/medicamentos")
    public ResponseEntity<List<MedicamentoResponseDTO>> listarMedicamentosDeTurno(
            @Parameter(description = "ID del turno", example = "1") @PathVariable Long id) {
        return ResponseEntity.ok(turnoService.listarMedicamentosDeTurno(id));
    }


    // POST /api/turnos/{turnoId}/medicamentos/{medicamentoId} -> receta un medicamento
    @Operation(summary = "Recetar un medicamento en un turno",
            description = "Asocia el medicamento al turno y descuenta una unidad de su stock. "
                    + "Si el medicamento no tiene stock la operacion se rechaza con 422.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Medicamento recetado; devuelve la receta completa"),
            @ApiResponse(responseCode = "404", description = "No existe el turno o el medicamento",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "422", description = "El medicamento no tiene stock disponible",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/{turnoId}/medicamentos/{medicamentoId}")
    public ResponseEntity<List<MedicamentoResponseDTO>> recetarMedicamento(
            @Parameter(description = "ID del turno", example = "1") @PathVariable Long turnoId,
            @Parameter(description = "ID del medicamento a recetar", example = "1")
            @PathVariable Long medicamentoId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(turnoService.recetarMedicamento(turnoId, medicamentoId));
    }
}
