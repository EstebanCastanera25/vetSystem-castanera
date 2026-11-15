package com.patitas.turno.adapter.out.feign;

import com.patitas.turno.domain.exception.ServicioNoDisponibleException;
import com.patitas.turno.domain.port.out.VeterinarioServicePort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeterinarioFeignAdapter implements VeterinarioServicePort {

    private final VeterinarioFeignClient client;

    // el circuit breaker "ms-veterinario" (configurado en ms-turno.yml) cuenta los fallos de esta llamada
    @Override
    @CircuitBreaker(name = "ms-veterinario", fallbackMethod = "existsByIdFallback")
    public boolean existsById(Long id) {
        try {
            client.findById(id);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    // se ejecuta cuando ms-veterinario falla o cuando el circuito esta abierto
    public boolean existsByIdFallback(Long id, Throwable causa) {
        throw new ServicioNoDisponibleException("ms-veterinario");
    }
}
