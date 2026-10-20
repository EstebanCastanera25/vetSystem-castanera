package com.patitas.duenio.domain.port.in;

import com.patitas.duenio.domain.model.Duenio;

import java.util.List;

// Puerto de entrada: lo que se puede hacer con los duenios (el QUE, no el COMO)
public interface DuenioService {

    List<Duenio> findAll();

    Duenio findById(Long id);

    Duenio save(Duenio duenio);

    Duenio update(Long id, Duenio duenio);

    void deleteById(Long id);
}
