package com.vetSystem.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.Turno;

@Mapper(componentModel = "spring")
public interface TurnoMapper {

    @Mapping(source = "mascota.id", target = "mascotaId")
    @Mapping(source = "mascota.nombre", target = "mascotaNombre")
    @Mapping(source = "veterinario.id", target = "veterinarioId")
    @Mapping(source = "veterinario.nombre", target = "veterinarioNombre")
    TurnoResponseDTO toDTO(Turno turno);
}
