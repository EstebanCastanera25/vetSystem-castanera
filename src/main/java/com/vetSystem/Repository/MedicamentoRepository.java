
package com.vetSystem.Repository;

import com.vetSystem.Entity.Medicamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface MedicamentoRepository extends
 JpaRepository<Medicamento, Long>{
    
    // Evita cargar dos veces el mismo medicamento con distinta capitalizacion.
    boolean existsByNombreIgnoreCase(String nombre);
}
