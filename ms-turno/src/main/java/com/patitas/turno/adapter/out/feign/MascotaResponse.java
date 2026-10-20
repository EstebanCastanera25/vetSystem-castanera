package com.patitas.turno.adapter.out.feign;

import lombok.Data;

// solo los campos que necesita ms-turno, no se comparte la clase de ms-mascota
@Data
public class MascotaResponse {
    private Long id;
    private String nombre;
    private Long duenioId;
}
