package com.patitas.auth.application;

import com.patitas.auth.domain.exception.CredencialesInvalidasException;
import com.patitas.auth.domain.exception.UsuarioYaExisteException;
import com.patitas.auth.domain.model.Usuario;
import com.patitas.auth.domain.port.in.AuthService;
import com.patitas.auth.domain.port.out.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    @Transactional
    public String register(String username, String password, String rol) {
        if (usuarioRepository.existsByUsername(username)) {
            throw new UsuarioYaExisteException(username);
        }
        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        // nunca se guarda la contrasenia en texto plano
        usuario.setPassword(passwordEncoder.encode(password));
        usuario.setRol(rol);
        usuarioRepository.save(usuario);
        return jwtService.generateToken(usuario.getUsername(), usuario.getRol());
    }

    @Override
    @Transactional(readOnly = true)
    public String login(String username, String password) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(CredencialesInvalidasException::new);
        // matches compara el texto con el hash guardado
        if (!passwordEncoder.matches(password, usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        return jwtService.generateToken(usuario.getUsername(), usuario.getRol());
    }
}
