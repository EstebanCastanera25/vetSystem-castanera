package com.patitas.auth.domain.port.in;

// puerto de entrada: registrarse y loguearse. Los dos devuelven el token JWT
public interface AuthService {

    String register(String username, String password, String rol);

    String login(String username, String password);
}
