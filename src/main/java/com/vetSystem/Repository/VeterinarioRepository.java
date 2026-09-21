package com.vetSystem.Repository;

import com.vetSystem.Entity.Veterinario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeterinarioRepository extends JpaRepository<Veterinario, Long> {

    // Query derivation: Spring genera el SQL leyendo el nombre del método
    // SELECT COUNT(*) > 0 FROM veterinarios WHERE matricula = ?
    boolean existsByMatricula(String matricula);

    // SELECT * FROM veterinarios WHERE matricula = ?
    Optional<Veterinario> findByMatricula(String matricula);

    // Buscador del frontend: UNA caja de texto contra CUATRO columnas.
    //
    // Es uno de los tres @Query escritos a mano del proyecto: uno por buscador (dueños,
    // mascotas y veterinarios). Todo el resto de las consultas son query derivation.
    // Acá la derivación no alcanza: el nombre equivalente sería
    // findByNombreContainingIgnoreCaseOrApellidoContainingIgnoreCaseOr... (136 caracteres)
    // y obligaría a pasarle CUATRO parámetros con el mismo valor. Con JPQL alcanza un solo :texto.
    //
    // LOWER() no es decorativo: MySQL compara sin distinguir mayúsculas por su collation,
    // pero H2 (la base de los tests) SÍ distingue. Sin LOWER() el buscador andaría a mano
    // y fallaría en mvnw test.
    //
    // SQL equivalente:
    // SELECT * FROM veterinarios
    //  WHERE LOWER(nombre)       LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(apellido)     LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(matricula)    LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(especialidad) LIKE LOWER(CONCAT('%', ?, '%'))
    // El email queda afuera: es un dato interno, no un criterio de búsqueda del recepcionista.
    //
    // OJO: la consulta NO lleva ORDER BY. El orden llega en el Pageable, y si acá hubiera
    // uno fijo, Spring le AGREGARÍA el del Pageable con una coma detrás: el fijo ganaría
    // siempre y el orden que pide el usuario quedaría de simple desempate. La tabla diría
    // "ordenado por matrícula" mostrando los datos ordenados por apellido.
    @Query("""
            SELECT v FROM Veterinario v
            WHERE LOWER(v.nombre)       LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(v.apellido)     LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(v.matricula)    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(v.especialidad) LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    Page<Veterinario> buscarPorTexto(@Param("texto") String texto, Pageable pageable);
}
