package com.patitas.auth.application;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

// crea y valida los tokens JWT (Header.Payload.Signature firmado con HS256)
@Service
public class JwtService {

    // 24 horas en milisegundos
    private static final long EXPIRACION_MS = 86400000L;

    private final SecretKey signingKey;

    // la clave viene de jwt.secret (ms-auth.yml del config-server)
    public JwtService(@Value("${jwt.secret}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(String username, String rol) {
        Date ahora = new Date();
        Date expira = new Date(ahora.getTime() + EXPIRACION_MS);
        return Jwts.builder()
                .subject(username)
                .claim("rol", rol)
                .issuedAt(ahora)
                .expiration(expira)
                .signWith(signingKey)
                .compact();
    }

    // true si la firma es correcta y no esta vencido
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return getClaims(token).getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser().verifyWith(signingKey).build().parseSignedClaims(token).getPayload();
    }
}
