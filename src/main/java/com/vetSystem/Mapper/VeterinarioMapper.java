package com.vetSystem.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.Entity.Veterinario;

@Mapper(componentModel = "spring")
public interface VeterinarioMapper {

    VeterinarioDTO toDTO(Veterinario veterinario);

    @Mapping(target = "turnos", ignore = true)
    Veterinario toEntity(VeterinarioDTO dto);
}
