package com.vetSystem.DTO;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MascotaDTO {

    private Long id;
    private String nombre;
    private String especie;
    private String raza;
    private LocalDate fechaNacimiento;
    private Long duenioId;
    private String duenioNombre;
}
