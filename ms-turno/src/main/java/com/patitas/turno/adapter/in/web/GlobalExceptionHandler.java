package com.patitas.turno.adapter.in.web;

import com.patitas.turno.domain.exception.MascotaNotFoundException;
import com.patitas.turno.domain.exception.TurnoNotFoundException;
import com.patitas.turno.domain.exception.VeterinarioNotFoundException;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // el id no existe -> 404
    @ExceptionHandler(TurnoNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(TurnoNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // la mascota no existe en ms-mascota -> 404
    @ExceptionHandler(MascotaNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMascota(MascotaNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // el veterinario no existe en ms-veterinario -> 404
    @ExceptionHandler(VeterinarioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleVeterinario(VeterinarioNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // el otro microservicio esta caido o tardo demasiado -> 503
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeign(FeignException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.SERVICE_UNAVAILABLE, "Servicio remoto no disponible: " + ex.getMessage(), request);
    }

    // fallo @Valid -> 400 con los mensajes de cada campo
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidacion(MethodArgumentNotValidException ex, HttpServletRequest request) {
        StringBuilder mensajes = new StringBuilder();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            if (mensajes.length() > 0) {
                mensajes.append("; ");
            }
            mensajes.append(error.getField()).append(": ").append(error.getDefaultMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, mensajes.toString(), request);
    }

    // el JSON del body no se puede leer -> 400
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleBodyInvalido(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.BAD_REQUEST, "El cuerpo del pedido tiene un formato invalido", request);
    }

    // la ruta no existe -> 404 (si no, caeria en el 500 de abajo)
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleRutaInexistente(NoResourceFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, "La ruta solicitada no existe", request);
    }

    // cualquier otro error que no esperabamos -> 500
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error inesperado en el servidor", request);
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String mensaje, HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                mensaje,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
