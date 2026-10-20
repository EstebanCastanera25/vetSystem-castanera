package com.patitas.duenio.adapter.in.web;

import com.patitas.duenio.domain.exception.DuenioNotFoundException;
import com.patitas.duenio.domain.exception.DuplicatedEmailException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // el id no existe -> 404
    @ExceptionHandler(DuenioNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(DuenioNotFoundException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // email repetido -> 409
    @ExceptionHandler(DuplicatedEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatedEmail(DuplicatedEmailException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // la base rechaza un dato repetido (ej: cedula) -> 409
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleIntegridad(DataIntegrityViolationException ex, HttpServletRequest request) {
        return buildResponse(HttpStatus.CONFLICT, "Ya existe un duenio con esos datos", request);
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
