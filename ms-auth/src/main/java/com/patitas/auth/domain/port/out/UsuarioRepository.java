package com.patitas.auth.domain.port.out;

import com.patitas.auth.domain.model.Usuario;

import java.util.Optional;

// puerto de salida: lo que el dominio necesita para guardar y buscar usuarios
public interface UsuarioRepository {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);

    Usuario save(Usuario usuario);
}
