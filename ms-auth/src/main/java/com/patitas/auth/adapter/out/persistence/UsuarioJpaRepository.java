package com.patitas.auth.adapter.out.persistence;

import com.patitas.auth.domain.model.Usuario;
import com.patitas.auth.domain.port.out.UsuarioRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

// adaptador de salida: spring data implementa el puerto UsuarioRepository
@Repository
public interface UsuarioJpaRepository extends JpaRepository<Usuario, Long>, UsuarioRepository {

    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
