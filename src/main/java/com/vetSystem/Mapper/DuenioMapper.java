package com.vetSystem.Mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.Entity.Duenio;

@Mapper(componentModel = "spring")
public interface DuenioMapper {

    DuenioDTO toDTO(Duenio duenio);

    @Mapping(target = "mascotas", ignore = true)
    Duenio toEntity(DuenioDTO dto);
}
