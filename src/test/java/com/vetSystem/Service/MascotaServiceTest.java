package com.vetSystem.Service;

import com.vetSystem.DTO.MascotaDTO;
import com.vetSystem.DTO.PaginaDTO;
import com.vetSystem.Entity.Duenio;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Mapper.MascotaMapper;
import com.vetSystem.Repository.DuenioRepository;
import com.vetSystem.Repository.MascotaRepository;
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

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Pruebas unitarias de MascotaService")
class MascotaServiceTest {

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private DuenioRepository duenioRepository;

    @Mock
    private MascotaMapper mascotaMapper;

    @InjectMocks
    private MascotaService mascotaService;

    @Test
    @DisplayName("Registrar normaliza los datos de la mascota antes de guardarlos")
    void registrarEntidad_cuandoLosDatosVienenEnMinusculas_losGuardaNormalizados() {
        // ARRANGE
        MascotaDTO dto = new MascotaDTO(null, "rocky", "perro", "caniche toy",
                LocalDate.of(2020, 5, 10), 1L, null);
        Duenio duenio = new Duenio();
        Mascota mascota = new Mascota();
        when(duenioRepository.findById(1L)).thenReturn(java.util.Optional.of(duenio));
        when(mascotaMapper.toEntity(dto)).thenReturn(mascota);
        when(mascotaRepository.save(mascota)).thenReturn(mascota);

        // ACT
        mascotaService.registrarEntidad(dto);

        // ASSERT
        assertThat(dto.getNombre()).isEqualTo("Rocky");
        assertThat(dto.getEspecie()).isEqualTo("Perro");
        assertThat(dto.getRaza()).isEqualTo("Caniche Toy");
        verify(mascotaMapper).toEntity(dto);
    }

    @Test
    @DisplayName("Buscar por texto retorna los DTO de las mascotas que coinciden")
    void buscarPorTexto_cuandoHayCoincidencias_retornaListaDeDTOs() {
        // ARRANGE
        Mascota primeraMascota = crearMascota(1L, "Rocky", "Perro", "Labrador");
        Mascota segundaMascota = crearMascota(2L, "Milo", "Perro", "Mestizo");
        MascotaDTO primerDTO = new MascotaDTO(1L, "Rocky", "Perro", "Labrador",
                LocalDate.of(2020, 5, 10), 1L, "Ana");
        MascotaDTO segundoDTO = new MascotaDTO(2L, "Milo", "Perro", "Mestizo",
                LocalDate.of(2020, 5, 10), 1L, "Ana");
        Page<Mascota> paginaDeEntidades = new PageImpl<>(List.of(primeraMascota, segundaMascota),
                PageRequest.of(0, 10), 2);
        when(mascotaRepository.buscarPorTexto(eq("perro"), any(Pageable.class)))
                .thenReturn(paginaDeEntidades);
        when(mascotaMapper.toDTO(primeraMascota)).thenReturn(primerDTO);
        when(mascotaMapper.toDTO(segundaMascota)).thenReturn(segundoDTO);

        // ACT
        PaginaDTO<MascotaDTO> resultado =
                mascotaService.buscarPorTexto("perro", 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).hasSize(2);
        assertThat(resultado.getContenido().get(0).getNombre()).isEqualTo("Rocky");
        assertThat(resultado.getContenido().get(1).getNombre()).isEqualTo("Milo");
        assertThat(resultado.getTotalElementos()).isEqualTo(2);
    }

    @Test
    @DisplayName("Buscar por texto retorna una lista vacía cuando no coincide ninguna mascota")
    void buscarPorTexto_cuandoNoHayCoincidencias_retornaListaVacia() {
        // ARRANGE
        when(mascotaRepository.buscarPorTexto(eq("zzzz"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // ACT
        PaginaDTO<MascotaDTO> resultado =
                mascotaService.buscarPorTexto("zzzz", 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).isEmpty();
        assertThat(resultado.getTotalElementos()).isZero();
        verifyNoInteractions(mascotaMapper);
    }

    @Test
    @DisplayName("Buscar con texto vacío lista todas las mascotas sin ir a la consulta LIKE")
    void buscarPorTexto_cuandoElTextoEsVacio_listaTodasLasMascotas() {
        // ARRANGE
        // OJO: findAll() y findAll(Pageable) son metodos DISTINTOS para Mockito.
        when(mascotaRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // ACT
        PaginaDTO<MascotaDTO> resultado =
                mascotaService.buscarPorTexto("", 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).isEmpty();
        verify(mascotaRepository).findAll(any(Pageable.class));
        verify(mascotaRepository, never()).buscarPorTexto(any(), any());
    }

    @Test
    @DisplayName("Buscar con texto null lista todas las mascotas")
    void buscarPorTexto_cuandoElTextoEsNull_listaTodasLasMascotas() {
        // ARRANGE
        // OJO: findAll() y findAll(Pageable) son metodos DISTINTOS para Mockito.
        when(mascotaRepository.findAll(any(Pageable.class))).thenReturn(Page.empty());

        // ACT
        PaginaDTO<MascotaDTO> resultado =
                mascotaService.buscarPorTexto(null, 0, 10, null, null);

        // ASSERT
        assertThat(resultado.getContenido()).isEmpty();
        verify(mascotaRepository).findAll(any(Pageable.class));
        verify(mascotaRepository, never()).buscarPorTexto(any(), any());
    }

    @Test
    @DisplayName("Buscar con espacios de más busca con el texto recortado")
    void buscarPorTexto_cuandoElTextoTieneEspacios_buscaConElTextoRecortado() {
        // ARRANGE
        when(mascotaRepository.buscarPorTexto(eq("rocky"), any(Pageable.class)))
                .thenReturn(Page.empty());

        // ACT
        mascotaService.buscarPorTexto("  rocky  ", 0, 10, null, null);

        // ASSERT
        verify(mascotaRepository).buscarPorTexto(eq("rocky"), any(Pageable.class));
    }

    private Mascota crearMascota(Long id, String nombre, String especie, String raza) {
        Duenio duenio = new Duenio();
        duenio.setId(1L);
        duenio.setNombre("Ana");
        duenio.setApellido("Gomez");
        duenio.setCedula("12345678");
        duenio.setTelefono(11111111);
        duenio.setEmail("ana@example.com");
        // Se deja mascotas en null para evitar el ciclo de equals entre Duenio y Mascota.

        Mascota mascota = new Mascota();
        mascota.setId(id);
        mascota.setNombre(nombre);
        mascota.setEspecie(especie);
        mascota.setRaza(raza);
        mascota.setFechaNacimiento(LocalDate.of(2020, 5, 10));
        mascota.setDuenio(duenio);
        return mascota;
    }
}
