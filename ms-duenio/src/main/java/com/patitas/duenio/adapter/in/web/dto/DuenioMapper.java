package com.patitas.duenio.adapter.in.web.dto;

import com.patitas.duenio.domain.model.Duenio;

import java.util.ArrayList;
import java.util.List;

public class DuenioMapper {

    private DuenioMapper() {
    }

    public static Duenio toEntity(DuenioRequest request) {
        Duenio duenio = new Duenio();
        duenio.setNombre(request.getNombre());
        duenio.setApellido(request.getApellido());
        duenio.setCedula(request.getCedula());
        duenio.setTelefono(request.getTelefono());
        duenio.setEmail(request.getEmail());
        return duenio;
    }

    public static DuenioResponse toResponse(Duenio duenio) {
        return new DuenioResponse(
                duenio.getId(),
                duenio.getNombre(),
                duenio.getApellido(),
                duenio.getCedula(),
                duenio.getTelefono(),
                duenio.getEmail());
    }

    public static List<DuenioResponse> toResponseList(List<Duenio> duenios) {
        List<DuenioResponse> respuesta = new ArrayList<>();
        for (Duenio duenio : duenios) {
            respuesta.add(toResponse(duenio));
        }
        return respuesta;
    }
}
