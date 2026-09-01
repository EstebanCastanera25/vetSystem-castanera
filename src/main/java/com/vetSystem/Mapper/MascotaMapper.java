package com.vetSystem.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.Entity.Mascota;

@Mapper(componentModel = "spring")
public interface MascotaMapper {

    @Mapping(source = "duenio.id", target = "duenioId")
    @Mapping(source = "duenio.nombre", target = "duenioNombre")
    MascotaDTO toDTO(Mascota mascota);

    // El Service resuelve el duenio a partir de duenioId.
    @Mapping(target = "duenio", ignore = true)
    Mascota toEntity(MascotaDTO dto);
}
