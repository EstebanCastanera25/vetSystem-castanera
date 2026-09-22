package com.vetSystem.Repository;

import com.vetSystem.Entity.Duenio;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DuenioRepository extends JpaRepository<Duenio, Long> {

    // Query derivation: Spring genera el SQL leyendo el nombre del método
    // SELECT COUNT(*) > 0 FROM duenios WHERE cedula = ?
    boolean existsByCedula(String cedula);

    // SELECT * FROM duenios WHERE email = ?
    Optional<Duenio> findByEmail(String email);

    // Usado por buscarPorString del contrato InterfaceService
    Optional<Duenio> findByNombre(String nombre);

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
    // SELECT * FROM duenios
    //  WHERE LOWER(nombre)   LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(apellido) LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(cedula)   LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(email)    LIKE LOWER(CONCAT('%', ?, '%'))
    //
    // El teléfono queda afuera: es Integer, y hacerle LIKE pide un CAST que no se comporta
    // igual en H2 que en MySQL.
    //
    // OJO: la consulta NO lleva ORDER BY. El orden llega en el Pageable, y si acá hubiera
    // uno fijo, Spring le AGREGARÍA el del Pageable con una coma detrás: el fijo ganaría
    // siempre y el orden que pide el usuario quedaría de simple desempate. La tabla diría
    // "ordenado por cédula" mostrando los datos ordenados por apellido.
    @Query("""
            SELECT d FROM Duenio d
            WHERE LOWER(d.nombre)   LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.apellido) LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.cedula)   LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.email)    LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    Page<Duenio> buscarPorTexto(@Param("texto") String texto, Pageable pageable);
}
