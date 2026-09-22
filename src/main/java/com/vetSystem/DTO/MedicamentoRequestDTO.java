
package com.vetSystem.DTO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Datos necesarios para registrar o modificar un medicamento")
@Data
@NoArgsConstructor
@AllArgsConstructor


public class MedicamentoRequestDTO {
    @Schema(description = "Nombre comercial del medicamento", example = "Amoxidal 500")
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @Schema(description = "Principio activo de la droga", example = "Amoxicilina")
    @NotBlank(message = "El principio activo es obligatorio")
    private String principioActivo;

    @Schema(description = "Unidades disponibles en la farmacia de la clinica", example = "25")
    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    private Integer stock;

    @Schema(description = "Precio de una unidad, en pesos", example = "4500.00")
    @NotNull(message = "El precio unitario es obligatorio")

    @DecimalMin(value = "0.0", inclusive = false,
            message = "El precio unitario debe ser mayor a cero")
    private BigDecimal precioUnitario;
}
