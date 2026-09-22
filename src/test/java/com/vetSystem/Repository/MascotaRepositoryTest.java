package com.vetSystem.Repository;

import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Prueba la consulta del buscador de mascotas contra una base DE VERDAD (H2 en memoria).
 *
 * <p>Con Mockito el repositorio es un mock y el {@code @Query} nunca se ejecuta. Por eso este
 * test de persistencia es necesario para comprobar que el JPQL y, en especial, el JOIN con el
 * dueño funcionan realmente.
 *
 * <p>{@code @DataJpaTest} levanta solamente la capa de persistencia y deshace los datos al
 * terminar cada test.
 */
@DataJpaTest
@DisplayName("MascotaRepository - la consulta del buscador contra H2")
class MascotaRepositoryTest {

    @Autowired
    private MascotaRepository mascotaRepository;

    @Autowired
    private DuenioRepository duenioRepository;

    @Test
    @DisplayName("Encuentra la mascota cuando el texto coincide con el apellido del dueño")
    void buscarPorTexto_cuandoElTextoCoincideConElApellidoDelDuenio_retornaLaMascota() {
        // ARRANGE
        Duenio duenio = duenioRepository.saveAndFlush(crearDuenio("Ana", "Gomez", "12345678"));
        mascotaRepository.saveAndFlush(crearMascota("Rocky", "Perro", "Labrador", duenio));

        // ACT
        Page<Mascota> resultado = mascotaRepository.buscarPorTexto("gomez",
                PageRequest.of(0, 10, Sort.by("nombre", "id")));

        // ASSERT: este es el test que prueba que el JOIN con el dueño funciona.
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Rocky");
    }

    @Test
    @DisplayName("Encuentra la mascota cuando el texto coincide con la especie")
    void buscarPorTexto_cuandoElTextoCoincideConLaEspecie_retornaLaMascota() {
        // ARRANGE
        Duenio duenio = duenioRepository.saveAndFlush(crearDuenio("Luis", "Perez", "87654321"));
        mascotaRepository.saveAndFlush(crearMascota("Milo", "Gato", null, duenio));

        // ACT
        Page<Mascota> resultado = mascotaRepository.buscarPorTexto("gato",
                PageRequest.of(0, 10, Sort.by("nombre", "id")));

        // ASSERT
        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).getNombre()).isEqualTo("Milo");
    }

    @Test
    @DisplayName("Cuenta todas las coincidencias aunque la página sea más chica que el total")
    void buscarPorTexto_cuandoLaPaginaEsMasChicaQueElTotal_cuentaTodasLasCoincidencias() {
        // ARRANGE
        Duenio duenio = duenioRepository.saveAndFlush(crearDuenio("Ana", "Gomez", "11223344"));
        mascotaRepository.saveAndFlush(crearMascota("Lola", "Perro", "Mestizo", duenio));
        mascotaRepository.saveAndFlush(crearMascota("Milo", "Gato", "Siames", duenio));
        mascotaRepository.saveAndFlush(crearMascota("Rocky", "Perro", "Labrador", duenio));

        // ACT
        Page<Mascota> resultado = mascotaRepository.buscarPorTexto("gomez",
                PageRequest.of(0, 2, Sort.by("nombre", "id")));

        // ASSERT: con Mockito el @Query nunca se ejecuta; este test es el único que prueba
        // que el conteo y el contenido miran lo mismo.
        assertThat(resultado.getContent()).hasSize(2);
        assertThat(resultado.getTotalElements()).isEqualTo(3);
        assertThat(resultado.getTotalPages()).isEqualTo(2);
    }

    private Duenio crearDuenio(String nombre, String apellido, String cedula) {
        Duenio duenio = new Duenio();
        duenio.setNombre(nombre);
        duenio.setApellido(apellido);
        duenio.setCedula(cedula);
        duenio.setTelefono(11111111);
        duenio.setEmail(nombre.toLowerCase() + "@example.com");
        // Se deja mascotas en null para evitar el equals/hashCode recursivo.
        return duenio;
    }

    private Mascota crearMascota(String nombre, String especie, String raza, Duenio duenio) {
        Mascota mascota = new Mascota();
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setFechaNacimiento(LocalDate.of(2020, 5, 10));
        mascota.setDuenio(duenio);
        return mascota;
    }
}
