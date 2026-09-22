package com.vetSystem.Repository;

import com.vetSystem.Entity.Duenio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la consulta del buscador contra una base DE VERDAD (H2 en memoria).
 *
 * <p>Por qué hace falta este test: en DuenioServiceTest el repositorio es un mock, así que
 * el {@code @Query} NUNCA se ejecuta. Si el JPQL mirara la columna equivocada, o le faltara
 * el {@code LOWER()}, esos tests pasarían igual. La única forma de probar la consulta es
 * corriéndola.
 *
 * <p>{@code @DataJpaTest} levanta SOLO la capa de persistencia: las entidades, los
 * repositorios y el DataSource. No levanta Controllers ni Services. Cada test corre dentro
 * de una transacción que se deshace al terminar, así que los dueños que se guardan acá no
 * quedan en ningún lado.
 *
 * <p>Corre sobre H2, igual que el resto: no necesita MySQL levantado.
 */
@DataJpaTest
@DisplayName("DuenioRepository - la consulta del buscador contra H2")
class DuenioRepositoryTest {

    @Autowired
    private DuenioRepository duenioRepository;

    // El orden va en el Pageable y NO adentro del @Query: si estuviera fijo en la consulta,
    // Spring le agregaria el del Pageable con una coma detras y el fijo ganaria siempre.
    private static final Pageable PRIMERA_PAGINA =
            PageRequest.of(0, 10, Sort.by("apellido", "nombre", "id"));

    @Test
    @DisplayName("Encuentra al dueño aunque el texto esté en otras mayúsculas")
    void buscarPorTexto_cuandoElTextoEstaEnOtraMayuscula_encuentraElDuenio() {
        // ARRANGE
        duenioRepository.saveAndFlush(crearDuenio("Luis", "Gomez", "12345678", "luis@example.com"));

        // ACT
        Page<Duenio> resultado = duenioRepository.buscarPorTexto("GOM", PRIMERA_PAGINA);

        // ASSERT: este es el test que justifica los LOWER() del JPQL. MySQL ignora las
        // mayúsculas por su collation, pero H2 no: sin LOWER() esta búsqueda no encontraría
        // nada y el buscador andaría en la demo pero fallaría en mvnw test.
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getApellido()).isEqualTo("Gomez");
    }

    @Test
    @DisplayName("Encuentra al dueño buscando un pedazo del email")
    void buscarPorTexto_cuandoElTextoEsParteDelEmail_encuentraElDuenio() {
        // ARRANGE: el texto no aparece ni en el nombre ni en el apellido ni en la cédula.
        duenioRepository.saveAndFlush(crearDuenio("Ana", "Perez", "87654321", "contacto@clinica.com"));

        // ACT
        Page<Duenio> resultado = duenioRepository.buscarPorTexto("clinica", PRIMERA_PAGINA);

        // ASSERT: prueba que el OR cubre las cuatro columnas, no sólo el nombre.
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getEmail()).isEqualTo("contacto@clinica.com");
    }

    @Test
    @DisplayName("Devuelve una lista vacía cuando el texto no coincide con ningún campo")
    void buscarPorTexto_cuandoNoCoincideConNingunCampo_retornaListaVacia() {
        // ARRANGE
        duenioRepository.saveAndFlush(crearDuenio("Ana", "Perez", "87654321", "ana@example.com"));

        // ACT
        Page<Duenio> resultado = duenioRepository.buscarPorTexto("zzzz", PRIMERA_PAGINA);

        // ASSERT
        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("La segunda pagina trae duenios distintos a los de la primera")
    void buscarPorTexto_cuandoSePideLaSegundaPagina_traeElementosDistintosALaPrimera() {
        // ARRANGE: tres duenios que coinciden con el mismo texto
        duenioRepository.saveAndFlush(crearDuenio("Ana", "Gomez", "11111111", "a@example.com"));
        duenioRepository.saveAndFlush(crearDuenio("Luis", "Gomez", "22222222", "b@example.com"));
        duenioRepository.saveAndFlush(crearDuenio("Sara", "Gomez", "33333333", "c@example.com"));
        Sort orden = Sort.by("apellido", "nombre", "id");

        // ACT: dos paginas de a dos
        Page<Duenio> primera = duenioRepository.buscarPorTexto("gomez", PageRequest.of(0, 2, orden));
        Page<Duenio> segunda = duenioRepository.buscarPorTexto("gomez", PageRequest.of(1, 2, orden));

        // ASSERT: esto prueba que el LIMIT/OFFSET va de VERDAD a la base (con un mock no se
        // probaria nada) y que el orden es estable entre paginas: sin un orden total, un
        // duenio podria aparecer en las dos paginas y otro en ninguna.
        assertThat(primera.getTotalElements()).isEqualTo(3);
        assertThat(primera.getTotalPages()).isEqualTo(2);
        assertThat(primera.getContent()).hasSize(2);
        assertThat(segunda.getContent()).hasSize(1);

        assertThat(primera.getContent().get(0).getNombre()).isEqualTo("Ana");
        assertThat(segunda.getContent().get(0).getNombre()).isEqualTo("Sara");
    }

    private Duenio crearDuenio(String nombre, String apellido, String cedula, String email) {
        Duenio duenio = new Duenio();
        // El id lo genera la base (GenerationType.IDENTITY), por eso no se setea.
        duenio.setNombre(nombre);
        duenio.setApellido(apellido);
        duenio.setCedula(cedula);
        duenio.setTelefono(11111111);
        duenio.setEmail(email);
        // Se deja mascotas en null: cargar los dos lados de la relación con @Data
        // dispara el equals/hashCode recursivo.
        return duenio;
    }
}
