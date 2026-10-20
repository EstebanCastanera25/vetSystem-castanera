package com.patitas.turno.application;

import com.patitas.turno.domain.exception.TurnoNotFoundException;
import com.patitas.turno.domain.model.EstadoTurno;
import com.patitas.turno.domain.model.Turno;
import com.patitas.turno.domain.port.in.TurnoService;
import com.patitas.turno.domain.port.out.TurnoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TurnoServiceImpl implements TurnoService {

    private final TurnoRepository turnoRepository;

    public TurnoServiceImpl(TurnoRepository turnoRepository) {
        this.turnoRepository = turnoRepository;
    }

    @Override
    public List<Turno> findAll() {
        return turnoRepository.findAll();
    }

    @Override
    public Turno findById(Long id) {
        Optional<Turno> turno = turnoRepository.findById(id);
        if (turno.isEmpty()) {
            throw new TurnoNotFoundException(id);
        }
        return turno.get();
    }

    @Override
    public List<Turno> findByVeterinarioId(Long veterinarioId) {
        return turnoRepository.findByVeterinarioId(veterinarioId);
    }

    @Override
    public List<Turno> findByMascotaId(Long mascotaId) {
        return turnoRepository.findByMascotaId(mascotaId);
    }

    @Override
    @Transactional
    public Turno save(Turno turno) {
        turno.setEstado(EstadoTurno.PENDIENTE);
        return turnoRepository.save(turno);
    }

    @Override
    @Transactional
    public Turno cancelar(Long id) {
        Turno turno = findById(id);
        turno.setEstado(EstadoTurno.CANCELADO);
        return turnoRepository.save(turno);
    }
}
