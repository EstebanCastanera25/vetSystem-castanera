package com.vetSystem.Service;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.DuenioMapper;
import com.vetSystem.Repository.DuenioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de DuenioService")
class DuenioServiceTest {

    @Mock
    private DuenioRepository duenioRepository;

    @Mock
    private DuenioMapper duenioMapper;

    @InjectMocks
    private DuenioService duenioService;

    @Test
    @DisplayName("Listar entidades retorna una lista vacía cuando no hay dueños")
    void listarEntidades_cuandoNoHayDuenios_retornaListaVacia() {
        // ARRANGE
        when(duenioRepository.findAll()).thenReturn(List.of());

        // ACT
        List<DuenioDTO> resultado = duenioService.listarEntidades();

        // ASSERT
        assertThat(resultado).isEmpty();
        verify(duenioRepository).findAll();
    }

    @Test
    @DisplayName("Listar entidades retorna los DTO cuando hay dueños")
    void listarEntidades_cuandoHayDuenios_retornaListaDeDTOs() {
        // ARRANGE
        Duenio primerDuenio = crearDuenio(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        Duenio segundoDuenio = crearDuenio(2L, "Luis", "Gómez", "87654321", 22222222,
                "luis@example.com");
        DuenioDTO primerDTO = new DuenioDTO(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        DuenioDTO segundoDTO = new DuenioDTO(2L, "Luis", "Gómez", "87654321", 22222222,
                "luis@example.com");
        when(duenioRepository.findAll()).thenReturn(List.of(primerDuenio, segundoDuenio));
        when(duenioMapper.toDTO(primerDuenio)).thenReturn(primerDTO);
        when(duenioMapper.toDTO(segundoDuenio)).thenReturn(segundoDTO);

        // ACT
        List<DuenioDTO> resultado = duenioService.listarEntidades();

        // ASSERT
        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).getNombre()).isEqualTo("Ana");
        assertThat(resultado.get(1).getNombre()).isEqualTo("Luis");
    }

    @Test
    @DisplayName("Buscar por id retorna el DTO cuando el dueño existe")
    void buscarPorId_cuandoExiste_retornaDTO() {
        // ARRANGE
        Duenio duenio = crearDuenio(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        DuenioDTO dto = new DuenioDTO(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        when(duenioRepository.findById(1L)).thenReturn(Optional.of(duenio));
        when(duenioMapper.toDTO(duenio)).thenReturn(dto);

        // ACT
        Optional<DuenioDTO> resultado = duenioService.buscarPorId(1L);

        // ASSERT
        assertThat(resultado)
                .isPresent()
                .get()
                .satisfies(duenioDTO -> {
                    assertThat(duenioDTO.getId()).isEqualTo(1L);
                    assertThat(duenioDTO.getNombre()).isEqualTo("Ana");
                });
    }

    @Test
    @DisplayName("Buscar por id retorna un Optional vacío cuando el dueño no existe")
    void buscarPorId_cuandoNoExiste_retornaOptionalVacio() {
        // ARRANGE
        when(duenioRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        Optional<DuenioDTO> resultado = duenioService.buscarPorId(99L);

        // ASSERT
        assertThat(resultado).isEmpty();
        verifyNoInteractions(duenioMapper);
    }

    @Test
    @DisplayName("Registrar entidad guarda y retorna el DTO cuando la cédula es nueva")
    void registrarEntidad_cuandoCedulaEsNueva_guardaYRetornaDTO() {
        // ARRANGE
        DuenioDTO entrada = new DuenioDTO(null, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        Duenio aGuardar = crearDuenio(null, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        Duenio guardado = crearDuenio(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        DuenioDTO salida = new DuenioDTO(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        when(duenioRepository.existsByCedula("12345678")).thenReturn(false);
        when(duenioMapper.toEntity(entrada)).thenReturn(aGuardar);
        when(duenioRepository.save(aGuardar)).thenReturn(guardado);
        when(duenioMapper.toDTO(guardado)).thenReturn(salida);

        // ACT
        DuenioDTO resultado = duenioService.registrarEntidad(entrada);

        // ASSERT
        assertThat(resultado).isSameAs(salida);
        assertThat(resultado.getId()).isEqualTo(1L);
        assertThat(resultado.getNombre()).isEqualTo("Ana");
        assertThat(resultado.getCedula()).isEqualTo("12345678");
        verify(duenioRepository, times(1)).save(aGuardar);
    }

    @Test
    @DisplayName("Registrar entidad lanza una excepción cuando la cédula está duplicada")
    void registrarEntidad_cuandoCedulaDuplicada_lanzaDuplicateResourceException() {
        // ARRANGE
        DuenioDTO entrada = new DuenioDTO(null, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        when(duenioRepository.existsByCedula("12345678")).thenReturn(true);

        // ACT
        // La invocación se realiza dentro de la aserción para capturar la excepción.

        // ASSERT
        assertThatThrownBy(() -> duenioService.registrarEntidad(entrada))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("12345678");
        // Una cédula duplicada debe cortar el flujo antes de persistir la entidad.
        verify(duenioRepository, never()).save(any(Duenio.class));
        verifyNoInteractions(duenioMapper);
    }

    @Test
    @DisplayName("Eliminar entidad lanza una excepción cuando el dueño no existe")
    void eliminarEntidad_cuandoNoExiste_lanzaResourceNotFoundException() {
        // ARRANGE
        when(duenioRepository.findById(99L)).thenReturn(Optional.empty());

        // ACT
        // La invocación se realiza dentro de la aserción para capturar la excepción.

        // ASSERT
        assertThatThrownBy(() -> duenioService.eliminarEntidad(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Duenio con id 99 no fue encontrado");
        // Si el dueño no existe, no hay una entidad válida que se pueda eliminar.
        verify(duenioRepository, never()).delete(any(Duenio.class));
    }

    private Duenio crearDuenio(Long id, String nombre, String apellido, String cedula,
                               Integer telefono, String email) {
        Duenio duenio = new Duenio();
        duenio.setId(id);
        duenio.setNombre(nombre);
        duenio.setApellido(apellido);
        duenio.setCedula(cedula);
        duenio.setTelefono(telefono);
        duenio.setEmail(email);
        // Se deja mascotas en null para evitar el ciclo de equals entre Duenio y Mascota.
        return duenio;
    }
}
