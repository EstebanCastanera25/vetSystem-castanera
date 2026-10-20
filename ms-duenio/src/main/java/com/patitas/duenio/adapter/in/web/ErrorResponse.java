package com.patitas.duenio.adapter.in.web;

import java.time.LocalDateTime;

public record ErrorResponse(LocalDateTime timestamp, int status, String error, String mensaje, String path) {
}
