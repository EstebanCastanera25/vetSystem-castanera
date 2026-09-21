package com.vetSystem.Service;

import com.vetSystem.DTO.DuenioDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Mapper.DuenioMapper;
import com.vetSystem.Repository.DuenioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

    @Test
    @DisplayName("Buscar por texto retorna los DTO de los dueños que coinciden")
    void buscarPorTexto_cuandoHayCoincidencias_retornaListaDeDTOs() {
        // ARRANGE
        Duenio primerDuenio = crearDuenio(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        Duenio segundoDuenio = crearDuenio(2L, "Mariana", "López", "87654321", 22222222,
                "mariana@example.com");
        DuenioDTO primerDTO = new DuenioDTO(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        DuenioDTO segundoDTO = new DuenioDTO(2L, "Mariana", "López", "87654321", 22222222,
                "mariana@example.com");
        Page<Duenio> paginaDeEntidades = new PageImpl<>(List.of(primerDuenio, segundoDuenio),
                PageRequest.of(0, 10), 2);
        when(duenioRepository.buscarPorTexto(eq("ana"), any(Pageable.class)))
                .thenReturn(paginaDeEntidades);
        when(duenioMapper.toDTO(primerDuenio)).thenReturn(primerDTO);
        when(duenioMapper.toDTO(segundoDuenio)).thenReturn(segundoDTO);

        // ACT
        PaginaDTO<DuenioDTO> resultado = duenioService.buscarPorTexto("ana", 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).hasSize(2);
        assertThat(resultado.getContenido().get(0).getNombre()).isEqualTo("Ana");
        assertThat(resultado.getContenido().get(1).getNombre()).isEqualTo("Mariana");
        assertThat(resultado.getTotalElementos()).isEqualTo(2);
    }

    @Test
    @DisplayName("Buscar por texto retorna una lista vacía cuando no coincide ningún dueño")
    void buscarPorTexto_cuandoNoHayCoincidencias_retornaListaVacia() {
        // ARRANGE
        when(duenioRepository.buscarPorTexto(eq("zzzz"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // ACT
        PaginaDTO<DuenioDTO> resultado = duenioService.buscarPorTexto("zzzz", 0, 10, null, null);

        // ASSERT: no encontrar nada no es un error, es una página vacía.
        assertThat(resultado.getContenido()).isEmpty();
        assertThat(resultado.getTotalElementos()).isZero();
        verifyNoInteractions(duenioMapper);
    }

    @Test
    @DisplayName("Buscar con texto vacío lista todos los dueños sin ir a la consulta LIKE")
    void buscarPorTexto_cuandoElTextoEsVacio_listaTodosLosDuenios() {
        // ARRANGE
        // OJO: findAll() y findAll(Pageable) son metodos DISTINTOS para Mockito.
        when(duenioRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // ACT
        PaginaDTO<DuenioDTO> resultado = duenioService.buscarPorTexto("", 0, 10, null, null);

        // ASSERT: la regla "sin texto = todos" vive en el service. Verificar el never() es
        // lo que prueba que no se le pide a la base un LIKE '%%' al pedo.
        assertThat(resultado.getContenido()).isEmpty();
        verify(duenioRepository).findAll(any(Pageable.class));
        verify(duenioRepository, never()).buscarPorTexto(any(), any());
    }

    @Test
    @DisplayName("Buscar con texto null lista todos los dueños (el parámetro es opcional)")
    void buscarPorTexto_cuandoElTextoEsNull_listaTodosLosDuenios() {
        // ARRANGE: cuando el frontend no manda ?buscar=, Spring le pasa null al controller.
        // OJO: findAll() y findAll(Pageable) son metodos DISTINTOS para Mockito.
        when(duenioRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // ACT
        PaginaDTO<DuenioDTO> resultado = duenioService.buscarPorTexto(null, 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).isEmpty();
        verify(duenioRepository).findAll(any(Pageable.class));
        verify(duenioRepository, never()).buscarPorTexto(any(), any());
    }

    @Test
    @DisplayName("Buscar con espacios de más busca con el texto recortado")
    void buscarPorTexto_cuandoElTextoTieneEspacios_buscaConElTextoRecortado() {
        // ARRANGE: sin el trim() la consulta sería LIKE '%  ana  %' y no encontraría nada,
        // así que un espacio de más parecería un buscador roto.
        when(duenioRepository.buscarPorTexto(eq("ana"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // ACT
        duenioService.buscarPorTexto("  ana  ", 0, 10, null, null);

        // ASSERT
        verify(duenioRepository).buscarPorTexto(eq("ana"), any(Pageable.class));
    }

    @Test
    @DisplayName("Registrar entidad guarda el nombre normalizado, venga como venga")
    void registrarEntidad_cuandoElNombreVieneEnMinusculas_loGuardaNormalizado() {
        // ARRANGE: el usuario escribe cualquier cosa
        DuenioDTO entrada = new DuenioDTO(null, "  juan   carlos ", "PEREZ", "12345678",
                11111111, "JUAN@Mail.COM");
        Duenio aGuardar = crearDuenio(null, "Juan Carlos", "Perez", "12345678", 11111111,
                "juan@mail.com");
        Duenio guardado = crearDuenio(1L, "Juan Carlos", "Perez", "12345678", 11111111,
                "juan@mail.com");
        DuenioDTO salida = new DuenioDTO(1L, "Juan Carlos", "Perez", "12345678", 11111111,
                "juan@mail.com");
        when(duenioRepository.existsByCedula("12345678")).thenReturn(false);
        when(duenioMapper.toEntity(any(DuenioDTO.class))).thenReturn(aGuardar);
        when(duenioRepository.save(aGuardar)).thenReturn(guardado);
        when(duenioMapper.toDTO(guardado)).thenReturn(salida);

        // ACT
        duenioService.registrarEntidad(entrada);

        // ASSERT: el service normaliza el DTO antes de pasárselo al mapper, así que se
        // comprueba sobre el mismo objeto que se le entregó.
        assertThat(entrada.getNombre()).isEqualTo("Juan Carlos");
        assertThat(entrada.getApellido()).isEqualTo("Perez");
        assertThat(entrada.getEmail()).isEqualTo("juan@mail.com");
        // La cédula no se toca: son dígitos
        assertThat(entrada.getCedula()).isEqualTo("12345678");
    }

    @Test
    @DisplayName("Modificar entidad también normaliza: no alcanza con hacerlo en el alta")
    void modificarEntidad_cuandoElNombreVieneEnMinusculas_loGuardaNormalizado() {
        // ARRANGE
        DuenioDTO entrada = new DuenioDTO(1L, "ana maría", "de la torre", "12345678",
                11111111, "ANA@Mail.com");
        Duenio existente = crearDuenio(1L, "Ana", "Pérez", "12345678", 11111111,
                "ana@example.com");
        when(duenioRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(duenioRepository.save(existente)).thenReturn(existente);
        when(duenioMapper.toDTO(existente)).thenReturn(new DuenioDTO());

        // ACT
        duenioService.modificarEntidad(entrada);

        // ASSERT: lo que importa es lo que quedó en la ENTIDAD que se manda a guardar.
        // El alta copia los campos con el mapper y la edición con setters: son dos caminos
        // distintos, por eso la normalización se prueba en los dos.
        assertThat(existente.getNombre()).isEqualTo("Ana María");
        assertThat(existente.getApellido()).isEqualTo("De La Torre");
        assertThat(existente.getEmail()).isEqualTo("ana@mail.com");
    }

    @Test
    @DisplayName("Los parámetros de paginación fuera de rango se recortan antes de ir a la base")
    void buscarPorTexto_cuandoLosParametrosSonInvalidos_recortaAntesDeIrALaBase() {
        // ARRANGE: página negativa y tamaño cero. Sin recorte, PageRequest.of() lanza
        // IllegalArgumentException y la API devolvería un 500 disparable desde la URL.
        when(duenioRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());
        ArgumentCaptor<Pageable> capturado = ArgumentCaptor.forClass(Pageable.class);

        // ACT
        duenioService.buscarPorTexto(null, -1, 0, null, null);

        // ASSERT: el pedido que efectivamente llegó al repositorio ya viene corregido
        verify(duenioRepository).findAll(capturado.capture());
        assertThat(capturado.getValue().getPageNumber()).isEqualTo(0);
        assertThat(capturado.getValue().getPageSize()).isEqualTo(10);
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
