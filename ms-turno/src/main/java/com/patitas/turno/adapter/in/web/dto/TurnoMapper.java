package com.patitas.turno.adapter.in.web.dto;

import com.patitas.turno.domain.model.Turno;

import java.util.ArrayList;
import java.util.List;

public class TurnoMapper {

    private TurnoMapper() {
    }

    public static Turno toEntity(TurnoRequest request) {
        Turno turno = new Turno();
        turno.setFechaHora(request.getFechaHora());
        turno.setMascotaId(request.getMascotaId());
        turno.setVeterinarioId(request.getVeterinarioId());
        turno.setMotivo(request.getMotivo());
        return turno;
    }

    public static TurnoResponse toResponse(Turno turno) {
        return new TurnoResponse(
                turno.getId(),
                turno.getFechaHora(),
                turno.getMascotaId(),
                turno.getVeterinarioId(),
                turno.getMotivo(),
                turno.getEstado());
    }

    public static List<TurnoResponse> toResponseList(List<Turno> turnos) {
        List<TurnoResponse> respuesta = new ArrayList<>();
        for (Turno turno : turnos) {
            respuesta.add(toResponse(turno));
        }
        return respuesta;
    }
}
