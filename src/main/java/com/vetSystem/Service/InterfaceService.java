package com.vetSystem.Service;

import java.util.List;
import java.util.Optional;

// Define las operaciones comunes de los servicios que trabajan con DTOs.
public interface InterfaceService<T> {

    T registrarEntidad(T t);

    Optional<T> buscarPorId(Long id);

    void eliminarEntidad(Long id);

    T modificarEntidad(T t);

    List<T> listarEntidades();

    Optional<T> buscarPorString(String valor);
}
