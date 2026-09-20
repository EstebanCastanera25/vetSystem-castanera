package com.vetSystem.Service;

import com.vetSystem.DTO.TurnoRequestDTO;
import com.vetSystem.DTO.TurnoResponseDTO;
import com.vetSystem.Entity.EstadoTurno;
import com.vetSystem.Entity.Mascota;
import com.vetSystem.Entity.Turno;
import com.vetSystem.Entity.Veterinario;
import com.vetSystem.Exception.ResourceNotFoundException;
import com.vetSystem.Exception.TurnoSuperpuestoException;
import com.vetSystem.Mapper.TurnoMapper;
import com.vetSystem.Repository.MascotaRepository;
import com.vetSystem.Repository.TurnoRepository;
import com.vetSystem.Repository.VeterinarioRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalTime;
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
@DisplayName("Tests unitarios de TurnoService")
class TurnoServiceTest {

    @Mock
    private TurnoRepository turnoRepository;

    @Mock
    private MascotaRepository mascotaRepository;

    @Mock
    private VeterinarioRepository veterinarioRepository;

    @Mock
    private TurnoMapper turnoMapper;

    @InjectMocks
    private TurnoService turnoService;

    private TurnoRequestDTO request;
    private Mascota mascota;
    private Veterinario veterinario;

    @BeforeEach
    void setUp() {
        mascota = new Mascota();
        mascota.setId(77L);
        mascota.setNombre("Luna");

        veterinario = new Veterinario();
        veterinario.setId(15L);
        veterinario.setNombre("Ana");
        veterinario.setApellido("Perez");

        request = new TurnoRequestDTO(
                LocalDate.now().plusDays(1),
                LocalTime.of(10, 30),
                "Control general",
                mascota.getId(),
                veterinario.getId());
    }

    @Test
    @DisplayName("Guarda un turno valido con estado pendiente")
    void crearTurno_cuandoDatosValidos_guardaTurnoConEstadoPendiente() {
        // ARRANGE
        Turno turnoGuardado = new Turno();
        turnoGuardado.setId(1L);
        turnoGuardado.setFecha(request.getFecha());
        turnoGuardado.setHora(request.getHora());
        turnoGuardado.setMotivo(request.getMotivo());
        turnoGuardado.setEstado(EstadoTurno.PENDIENTE);
        turnoGuardado.setMascota(mascota);
        turnoGuardado.setVeterinario(veterinario);

        TurnoResponseDTO responseDTO = new TurnoResponseDTO(
                1L,
                request.getFecha(),
                request.getHora(),
                request.getMotivo(),
                EstadoTurno.PENDIENTE,
                null,
                mascota.getId(),
                mascota.getNombre(),
                veterinario.getId(),
                veterinario.getNombre());

        when(mascotaRepository.findById(request.getMascotaId())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(request.getVeterinarioId())).thenReturn(Optional.of(veterinario));
        when(turnoRepository.existsByVeterinarioIdAndFechaAndHora(
                request.getVeterinarioId(), request.getFecha(), request.getHora())).thenReturn(false);
        when(turnoRepository.save(any(Turno.class))).thenReturn(turnoGuardado);
        when(turnoMapper.toDTO(turnoGuardado)).thenReturn(responseDTO);

        // ACT
        TurnoResponseDTO resultado = turnoService.crearTurno(request);

        // ASSERT
        assertThat(resultado).isSameAs(responseDTO);

        // Se captura el Turno que el service armo internamente para inspeccionarlo:
        // el captor ya verifica que save() se llamo exactamente una vez.
        ArgumentCaptor<Turno> turnoCaptor = ArgumentCaptor.forClass(Turno.class);
        verify(turnoRepository, times(1)).save(turnoCaptor.capture());
        Turno turnoEnviado = turnoCaptor.getValue();

        assertThat(turnoEnviado.getEstado()).isEqualTo(EstadoTurno.PENDIENTE);
        assertThat(turnoEnviado.getFecha()).isEqualTo(request.getFecha());
        assertThat(turnoEnviado.getHora()).isEqualTo(request.getHora());
        assertThat(turnoEnviado.getMotivo()).isEqualTo(request.getMotivo());
        assertThat(turnoEnviado.getMascota()).isSameAs(mascota);
        assertThat(turnoEnviado.getVeterinario()).isSameAs(veterinario);
    }

    @Test
    @DisplayName("Lanza TurnoSuperpuestoException cuando el horario ya esta ocupado")
    void crearTurno_cuandoHaySuperposicion_lanzaTurnoSuperpuestoException() {
        // ARRANGE
        when(mascotaRepository.findById(request.getMascotaId())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(request.getVeterinarioId())).thenReturn(Optional.of(veterinario));
        when(turnoRepository.existsByVeterinarioIdAndFechaAndHora(
                request.getVeterinarioId(), request.getFecha(), request.getHora())).thenReturn(true);

        // ACT
        // ASSERT
        assertThatThrownBy(() -> turnoService.crearTurno(request))
                .isInstanceOf(TurnoSuperpuestoException.class)
                .hasMessageContaining(request.getHora().toString());

        // La regla de negocio debe impedir la escritura para no dejar rastros en la base de datos.
        verify(turnoRepository, never()).save(any(Turno.class));
        verifyNoInteractions(turnoMapper);
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException cuando la mascota no existe")
    void crearTurno_cuandoMascotaNoExiste_lanzaResourceNotFoundException() {
        // ARRANGE
        when(mascotaRepository.findById(request.getMascotaId())).thenReturn(Optional.empty());

        // ACT
        // ASSERT
        assertThatThrownBy(() -> turnoService.crearTurno(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Mascota con id 77 no fue encontrado");

        // El service corta en la primera validacion: ni siquiera consulta el veterinario.
        verify(turnoRepository, never()).save(any(Turno.class));
        verifyNoInteractions(veterinarioRepository, turnoMapper);
    }

    @Test
    @DisplayName("Lanza ResourceNotFoundException cuando el veterinario no existe")
    void crearTurno_cuandoVeterinarioNoExiste_lanzaResourceNotFoundException() {
        // ARRANGE
        when(mascotaRepository.findById(request.getMascotaId())).thenReturn(Optional.of(mascota));
        when(veterinarioRepository.findById(request.getVeterinarioId())).thenReturn(Optional.empty());

        // ACT
        // ASSERT
        assertThatThrownBy(() -> turnoService.crearTurno(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Veterinario con id 15 no fue encontrado");

        verify(turnoRepository, never()).save(any(Turno.class));
        verifyNoInteractions(turnoMapper);
    }
}
