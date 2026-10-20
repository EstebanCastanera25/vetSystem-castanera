package com.patitas.gateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Instant;

// filtro global: loguea todas las peticiones que pasan por el gateway
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    private static final Logger log = LoggerFactory.getLogger(LoggingFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = Instant.now().toEpochMilli();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().toString();

        log.info("[GATEWAY] --> {} {}", method, path);

        // el gateway es reactivo: el log de la respuesta se hace cuando termina la cadena
        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long duration = Instant.now().toEpochMilli() - start;
            HttpStatusCode status = exchange.getResponse().getStatusCode();
            int codigo = 0;
            if (status != null) {
                codigo = status.value();
            }
            log.info("[GATEWAY] <-- {} {} {} {}ms", method, path, codigo, duration);
        }));
    }

    @Override
    public int getOrder() {
        // -1 para que corra antes que los demas filtros
        return -1;
    }
}
