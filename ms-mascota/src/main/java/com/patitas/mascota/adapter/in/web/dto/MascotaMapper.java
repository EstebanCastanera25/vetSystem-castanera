package com.patitas.mascota.adapter.in.web.dto;

import com.patitas.mascota.domain.model.Mascota;

import java.util.ArrayList;
import java.util.List;

public class MascotaMapper {

    private MascotaMapper() {
    }

    public static Mascota toEntity(MascotaRequest request) {
        Mascota mascota = new Mascota();
        mascota.setNombre(request.getNombre());
        mascota.setEspecie(request.getEspecie());
        mascota.setRaza(request.getRaza());
        mascota.setFechaNacimiento(request.getFechaNacimiento());
        mascota.setDuenioId(request.getDuenioId());
        return mascota;
    }

    public static MascotaResponse toResponse(Mascota mascota) {
        return new MascotaResponse(
                mascota.getId(),
                mascota.getNombre(),
                mascota.getEspecie(),
                mascota.getRaza(),
                mascota.getFechaNacimiento(),
                mascota.getDuenioId());
    }

    public static List<MascotaResponse> toResponseList(List<Mascota> mascotas) {
        List<MascotaResponse> respuesta = new ArrayList<>();
        for (Mascota mascota : mascotas) {
            respuesta.add(toResponse(mascota));
        }
        return respuesta;
    }
}
