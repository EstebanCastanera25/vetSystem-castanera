package com.patitas.turno.adapter.out.feign;

import com.patitas.turno.domain.exception.ServicioNoDisponibleException;
import com.patitas.turno.domain.port.out.MascotaServicePort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MascotaFeignAdapter implements MascotaServicePort {

    private final MascotaFeignClient client;

    // el circuit breaker "ms-mascota" (configurado en ms-turno.yml) cuenta los fallos de esta llamada
    @Override
    @CircuitBreaker(name = "ms-mascota", fallbackMethod = "existsByIdFallback")
    public boolean existsById(Long id) {
        // si ms-mascota responde 404 la mascota no existe: no es un fallo del servicio
        try {
            client.findById(id);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    // se ejecuta cuando ms-mascota falla o cuando el circuito esta abierto
    // misma firma que existsById + el Throwable al final
    public boolean existsByIdFallback(Long id, Throwable causa) {
        throw new ServicioNoDisponibleException("ms-mascota");
    }
}
