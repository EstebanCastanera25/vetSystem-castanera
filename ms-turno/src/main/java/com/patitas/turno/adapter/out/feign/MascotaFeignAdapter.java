package com.patitas.turno.adapter.out.feign;

import com.patitas.turno.domain.port.out.MascotaServicePort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MascotaFeignAdapter implements MascotaServicePort {

    private final MascotaFeignClient client;

    @Override
    public boolean existsById(Long id) {
        // si ms-mascota responde 404 la mascota no existe, los otros errores siguen de largo
        try {
            client.findById(id);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
}
