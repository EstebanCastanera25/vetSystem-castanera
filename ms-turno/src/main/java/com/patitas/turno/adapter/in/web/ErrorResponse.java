package com.patitas.turno.adapter.in.web;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String mensaje, String path) {
}
