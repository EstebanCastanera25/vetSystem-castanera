package com.patitas.gateway.filters;

import com.patitas.gateway.security.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

// filtro global: ninguna peticion llega a los microservicios sin un token valido
// el gateway es reactivo (webflux): se usa ServerWebExchange, no HttpServletRequest
@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private final JwtService jwtService;

    public AuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().value();

        // login y registro son publicos: si no, no habria forma de conseguir el primer token
        if (path.startsWith("/auth/")) {
            return chain.filter(exchange);
        }

        // el navegador manda un OPTIONS (preflight de CORS) antes del pedido real y sin token
        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        // tiene que venir "Authorization: Bearer <token>"
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return rechazar(exchange);
        }

        // se saca "Bearer " (7 caracteres) y queda el token
        String token = authHeader.substring(7);
        if (!jwtService.validateToken(token)) {
            return rechazar(exchange);
        }

        // token valido: se le pasa al microservicio quien hace el pedido
        ServerHttpRequest request = exchange.getRequest().mutate()
                .header("X-Auth-User", jwtService.extractUsername(token))
                .header("X-Auth-Rol", jwtService.extractRol(token))
                .build();
        return chain.filter(exchange.mutate().request(request).build());
    }

    // 401 sin llegar al microservicio
    private Mono<Void> rechazar(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        // 0: corre despues del LoggingFilter (-1), asi tambien se loguean los 401
        return 0;
    }
}
