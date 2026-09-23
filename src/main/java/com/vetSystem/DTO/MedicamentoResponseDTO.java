
package com.vetSystem.DTO;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Informacion de un medicamento de la farmacia de la clinica")
@Data
@NoArgsConstructor
@AllArgsConstructor


public class MedicamentoResponseDTO {
    @Schema(description = "Identificador unico del medicamento, generado por el servidor",
            example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Long id;

    @Schema(description = "Nombre comercial del medicamento", example = "Amoxidal 500",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String nombre;

    @Schema(description = "Principio activo de la droga", example = "Amoxicilina",
            accessMode = Schema.AccessMode.READ_ONLY)
    private String principioActivo;

    @Schema(description = "Unidades que quedan disponibles", example = "24",
            accessMode = Schema.AccessMode.READ_ONLY)
    private Integer stock;

    @Schema(description = "Precio de una unidad, en pesos", example = "4500.00",
            accessMode = Schema.AccessMode.READ_ONLY)
    private BigDecimal precioUnitario;
}
