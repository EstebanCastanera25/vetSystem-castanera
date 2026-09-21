package com.vetSystem.Service;

import com.vetSystem.DTO.VeterinarioDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.DuplicateResourceException;
import com.vetSystem.Mapper.VeterinarioMapper;
import com.vetSystem.Repository.VeterinarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de VeterinarioService")
class VeterinarioServiceTest {

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private VeterinarioMapper veterinarioMapper;

    @InjectMocks
    private VeterinarioService veterinarioService;

    @Test
    @DisplayName("Registrar normaliza los datos del veterinario antes de guardarlos")
    void registrarEntidad_cuandoElNombreVieneEnMinusculas_loGuardaNormalizado() {
        // ARRANGE
        VeterinarioDTO dto = new VeterinarioDTO(null, "ana  maria", "PEREZ", "mv-1001",
                "clinica general", "ANA@Mail.COM");
        Veterinario veterinario = new Veterinario();
        when(veterinarioMapper.toEntity(dto)).thenReturn(veterinario);
        when(veterinarioRepository.save(veterinario)).thenReturn(veterinario);

        // ACT
        veterinarioService.registrarEntidad(dto);

        // ASSERT
        assertThat(dto.getNombre()).isEqualTo("Ana Maria");
        assertThat(dto.getApellido()).isEqualTo("Perez");
        assertThat(dto.getEspecialidad()).isEqualTo("Clinica General");
        assertThat(dto.getEmail()).isEqualTo("ana@mail.com");
        assertThat(dto.getMatricula()).isEqualTo("MV-1001");
        verify(veterinarioMapper).toEntity(dto);
    }

    @Test
    @DisplayName("Registrar detecta una matricula duplicada aunque llegue en minusculas")
    void registrarEntidad_cuandoLaMatriculaDuplicadaVieneEnMinusculas_lanzaDuplicateResourceException() {
        // ARRANGE
        VeterinarioDTO dto = new VeterinarioDTO(null, "Ana", "Perez", "mv-5501",
                "Clinica General", "ana@mail.com");
        when(veterinarioRepository.existsByMatricula("MV-5501")).thenReturn(true);

        // ACT
        assertThrows(DuplicateResourceException.class,
                () -> veterinarioService.registrarEntidad(dto));

        // ASSERT
        verify(veterinarioRepository, never()).save(any());
    }

    @Test
    @DisplayName("Buscar por texto retorna los DTO de los veterinarios que coinciden")
    void buscarPorTexto_cuandoHayCoincidencias_retornaListaDeDTOs() {
        // ARRANGE
        Veterinario primerVeterinario = crearVeterinario(1L, "Ana", "Pérez", "MV-1001",
                "Clínica General", "ana@example.com");
        Veterinario segundoVeterinario = crearVeterinario(2L, "Mariana", "López", "MV-1002",
                "Cardiología", "mariana@example.com");
        VeterinarioDTO primerDTO = new VeterinarioDTO(1L, "Ana", "Pérez", "MV-1001",
                "Clínica General", "ana@example.com");
        VeterinarioDTO segundoDTO = new VeterinarioDTO(2L, "Mariana", "López", "MV-1002",
                "Cardiología", "mariana@example.com");
        Page<Veterinario> paginaDeEntidades = new PageImpl<>(List.of(primerVeterinario, segundoVeterinario),
                PageRequest.of(0, 10), 2);
        when(veterinarioRepository.buscarPorTexto(eq("ana"), any(Pageable.class)))
                .thenReturn(paginaDeEntidades);
        when(veterinarioMapper.toDTO(primerVeterinario)).thenReturn(primerDTO);
        when(veterinarioMapper.toDTO(segundoVeterinario)).thenReturn(segundoDTO);

        // ACT
        PaginaDTO<VeterinarioDTO> resultado =
                veterinarioService.buscarPorTexto("ana", 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).hasSize(2);
        assertThat(resultado.getContenido().get(0).getNombre()).isEqualTo("Ana");
        assertThat(resultado.getContenido().get(1).getNombre()).isEqualTo("Mariana");
        assertThat(resultado.getTotalElementos()).isEqualTo(2);
    }

    @Test
    @DisplayName("Buscar por texto retorna una lista vacía cuando no coincide ningún veterinario")
    void buscarPorTexto_cuandoNoHayCoincidencias_retornaListaVacia() {
        // ARRANGE
        when(veterinarioRepository.buscarPorTexto(eq("zzzz"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // ACT
        PaginaDTO<VeterinarioDTO> resultado =
                veterinarioService.buscarPorTexto("zzzz", 0, 10, null, null);

        // ASSERT: no encontrar nada no es un error, es una lista vacía.
        assertThat(resultado.getContenido()).isEmpty();
        assertThat(resultado.getTotalElementos()).isZero();
        verifyNoInteractions(veterinarioMapper);
    }

    @Test
    @DisplayName("Buscar con texto vacío lista todos los veterinarios sin ir a la consulta LIKE")
    void buscarPorTexto_cuandoElTextoEsVacio_listaTodosLosVeterinarios() {
        // ARRANGE
        when(veterinarioRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // ACT
        PaginaDTO<VeterinarioDTO> resultado =
                veterinarioService.buscarPorTexto("", 0, 10, null, null);

        // ASSERT: la regla "sin texto = todos" vive en el service. Verificar el never() es
        // lo que prueba que no se le pide a la base un LIKE '%%' al pedo.
        assertThat(resultado.getContenido()).isEmpty();
        verify(veterinarioRepository).findAll(any(Pageable.class));
        verify(veterinarioRepository, never()).buscarPorTexto(any(), any());
    }

    @Test
    @DisplayName("Buscar con texto null lista todos los veterinarios (el parámetro es opcional)")
    void buscarPorTexto_cuandoElTextoEsNull_listaTodosLosVeterinarios() {
        // ARRANGE: cuando el frontend no manda ?buscar=, Spring le pasa null al controller.
        when(veterinarioRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // ACT
        PaginaDTO<VeterinarioDTO> resultado =
                veterinarioService.buscarPorTexto(null, 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).isEmpty();
        verify(veterinarioRepository).findAll(any(Pageable.class));
        verify(veterinarioRepository, never()).buscarPorTexto(any(), any());
    }

    @Test
    @DisplayName("Buscar con espacios de más busca con el texto recortado")
    void buscarPorTexto_cuandoElTextoTieneEspacios_buscaConElTextoRecortado() {
        // ARRANGE: sin el trim() la consulta sería LIKE '%  ana  %' y no encontraría nada,
        // así que un espacio de más parecería un buscador roto.
        when(veterinarioRepository.buscarPorTexto(eq("ana"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // ACT
        veterinarioService.buscarPorTexto("  ana  ", 0, 10, null, null);

        // ASSERT
        verify(veterinarioRepository).buscarPorTexto(eq("ana"), any(Pageable.class));
    }

    private Veterinario crearVeterinario(Long id, String nombre, String apellido, String matricula,
                                         String especialidad, String email) {
        Veterinario veterinario = new Veterinario();
        veterinario.setId(id);
        veterinario.setNombre(nombre);
        veterinario.setApellido(apellido);
        veterinario.setMatricula(matricula);
        veterinario.setEspecialidad(especialidad);
        veterinario.setEmail(email);
        // Se deja turnos en null para evitar el ciclo de equals entre Veterinario y Turno.
        return veterinario;
    }
}
