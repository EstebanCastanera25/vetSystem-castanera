package com.vetSystem.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VeterinarioDTO {

    private Long id;
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
    @NotBlank(message = "El apellido es obligatorio")
    private String apellido;
    @NotBlank(message = "La matrícula es obligatoria")
    @Pattern(regexp = "MV-\\d+", message = "La matrícula debe tener el formato MV-XXXX")
    private String matricula;
    @NotBlank(message = "La especialidad es obligatoria")
    private String especialidad;
    @Email(message = "El email debe tener un formato válido")
    private String email;
}
