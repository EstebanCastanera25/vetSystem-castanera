package com.patitas.veterinario.adapter.in.web.dto;

import com.patitas.veterinario.domain.model.Veterinario;

import java.util.ArrayList;
import java.util.List;

public class VeterinarioMapper {

    private VeterinarioMapper() {
    }

    public static Veterinario toEntity(VeterinarioRequest request) {
        Veterinario veterinario = new Veterinario();
        veterinario.setNombre(request.getNombre());
        veterinario.setApellido(request.getApellido());
        veterinario.setMatricula(request.getMatricula());
        veterinario.setEspecialidad(request.getEspecialidad());
        veterinario.setEmail(request.getEmail());
        return veterinario;
    }

    public static VeterinarioResponse toResponse(Veterinario veterinario) {
        return new VeterinarioResponse(
                veterinario.getId(),
                veterinario.getNombre(),
                veterinario.getApellido(),
                veterinario.getMatricula(),
                veterinario.getEspecialidad(),
                veterinario.getEmail());
    }

    public static List<VeterinarioResponse> toResponseList(List<Veterinario> veterinarios) {
        List<VeterinarioResponse> respuesta = new ArrayList<>();
        for (Veterinario veterinario : veterinarios) {
            respuesta.add(toResponse(veterinario));
        }
        return respuesta;
    }
}
