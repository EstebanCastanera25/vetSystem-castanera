package com.vetSystem.Exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

// Manejo centralizado de errores: intercepta las excepciones de TODOS los
// controllers y devuelve siempre un ErrorResponse con la misma estructura.
// Regla: error del cliente → 4xx, error del servidor → 5xx.
@Slf4j  // Lombok: genera el logger "log"
@RestControllerAdvice
public class GlobalExceptionHandler {
    // El medicamento no tiene stock para recetar -> HTTP 422.
    // REGLA DE NEGOCIO Tampoco es un 409: no hay conflicto con el estado del recurso,
    // hay una precondicion del dominio que no se cumple.
    @ExceptionHandler(StockInsuficienteException.class)
    public ResponseEntity<ErrorResponse> handleStockInsuficiente(StockInsuficienteException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

     // El duenio ya llego al cupo de mascotas -> HTTP 422, por el mismo motivo.
    @ExceptionHandler(CupoExcedidoException.class)
    public ResponseEntity<ErrorResponse> handleCupoExcedido(CupoExcedidoException ex,
                                                            HttpServletRequest request) {
        return buildResponse(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    // Bean Validation falló (@Valid) → HTTP 400 con los mensajes de todos los campos
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex,
                                                                HttpServletRequest request) {
        StringBuilder mensajes = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (mensajes.length() > 0) {
                mensajes.append("; ");
            }
            mensajes.append(error.getField()).append(": ").append(error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, mensajes.toString(), request);
    }

    // El JSON del body no se pudo leer (mal formado, o un número fuera de rango) → HTTP 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBodyIlegible(HttpMessageNotReadableException ex,
                                                            HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "El cuerpo del pedido tiene un formato inválido", request);
    }

    // Un parámetro de la URL no se pudo convertir al tipo que espera el controller → HTTP 400.
    // Pasa, por ejemplo, con ?estado=BASURA (no es un valor del enum EstadoTurno) o con
    // /api/duenios/abc (no es un Long). Sin este handler caían en el Exception genérico y la
    // API devolvía un 500, como si el error fuera del servidor y no del pedido.
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleParametroInvalido(MethodArgumentTypeMismatchException ex,
                                                                 HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST,
                "El valor del parámetro '" + ex.getName() + "' no es válido", request);
    }

    // Un ID no existe en la base → HTTP 404
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // La URL pedida no existe en la API → HTTP 404 (sin esto caía en el 500 genérico)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleUrlInexistente(NoResourceFoundException ex,
                                                              HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request);
    }

    // Recurso duplicado (cédula, matrícula) → HTTP 409
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
                                                         HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // El veterinario ya tiene turno en ese horario → HTTP 409
    @ExceptionHandler(TurnoSuperpuestoException.class)
    public ResponseEntity<ErrorResponse> handleTurnoSuperpuesto(TurnoSuperpuestoException ex,
                                                                HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // La base rechazó la operación por una restricción (ej: borrar una mascota que
    // tiene turnos) → HTTP 409. El mensaje es fijo: ex.getMessage() expondría el SQL.
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex,
                                                          HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT,
                "No se puede completar la operación: el registro tiene datos asociados", request);
    }

    // Último recurso: cualquier error inesperado del sistema → HTTP 500.
    // Se loguea porque al cliente no se le muestra el detalle.
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado en el servidor", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String mensaje,
                                                        HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
