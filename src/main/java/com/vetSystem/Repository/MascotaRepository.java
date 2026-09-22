package com.vetSystem.Repository;

import com.vetSystem.Entity.Mascota;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {

    // Todas las mascotas de un dueño específico
    // SELECT * FROM mascotas WHERE duenio_id = ?
    List<Mascota> findByDuenioId(Long duenioId);

    // Verificar si existe una mascota con ese nombre para ese dueño
    boolean existsByNombreAndDuenioId(String nombre, Long duenioId);

    // Contar mascotas por especie (útil para reportes futuros)
    long countByEspecie(String especie);

    // Usado por buscarPorString del contrato InterfaceService
    Optional<Mascota> findByNombreIgnoreCase(String nombre);

    // Buscador del frontend: una caja de texto contra datos de la mascota y de su dueño.
    //
    // SQL equivalente:
    // SELECT m.* FROM mascotas m
    // JOIN duenios d ON d.id = m.duenio_id
    //  WHERE LOWER(m.nombre)   LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(m.especie)  LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(m.raza)     LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(d.nombre)   LIKE LOWER(CONCAT('%', ?, '%'))
    //     OR LOWER(d.apellido) LIKE LOWER(CONCAT('%', ?, '%'))
    //
    // También se busca por nombre y apellido del DUEÑO, por eso la consulta necesita un JOIN.
    // Es JOIN FETCH y no LEFT JOIN porque duenio_id es NOT NULL: toda mascota tiene dueño.
    // El FETCH trae al dueño en la MISMA consulta, así el mapper puede armar duenioNombre sin
    // disparar un SELECT por cada fila (el problema N+1).
    // Como duenio es una relación @ManyToOne, el fetch NO multiplica las filas y no hace falta
    // usar DISTINCT.
    //
    // OJO: la consulta ya NO lleva ORDER BY. El orden llega en el Pageable. Si quedara uno
    // fijo, Spring le agregaría el del Pageable con una coma detrás y el fijo ganaría siempre
    // (bug silencioso).
    //
    // El conteo usa JOIN sin FETCH: para contar no hace falta traer al dueño, pero sí el join,
    // porque el WHERE pregunta por d.nombre y d.apellido. Y el conteo nunca lleva ORDER BY.
    //
    // El WHERE del conteo tiene que ser idéntico al del contenido: si se desincronizan,
    // totalElementos miente y el paginador muestra páginas que no existen.
    @Query(value = """
            SELECT m FROM Mascota m JOIN FETCH m.duenio d
            WHERE LOWER(m.nombre)    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(m.especie)   LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(m.raza)      LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.nombre)    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.apellido)  LIKE LOWER(CONCAT('%', :texto, '%'))
            """,
            countQuery = """
            SELECT COUNT(m) FROM Mascota m JOIN m.duenio d
            WHERE LOWER(m.nombre)    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(m.especie)   LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(m.raza)      LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.nombre)    LIKE LOWER(CONCAT('%', :texto, '%'))
               OR LOWER(d.apellido)  LIKE LOWER(CONCAT('%', :texto, '%'))
            """)
    Page<Mascota> buscarPorTexto(@Param("texto") String texto, Pageable pageable);
}
