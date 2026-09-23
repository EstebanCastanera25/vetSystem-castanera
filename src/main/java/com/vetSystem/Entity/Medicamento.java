
package com.vetSystem.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;


@Entity
@Table(name = "medicamentos")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Medicamento {

     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    
    @Column(nullable = false)
    private String nombre;
    
    @Column(name = "principio_activo", nullable = false)
    private String principioActivo;
    
    @Column(nullable = false)
    private Integer stock;

    @Column(name = "precio_unitario", nullable = false, scale = 2)
    private BigDecimal precioUnitario;
    
}
