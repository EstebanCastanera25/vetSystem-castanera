package com.patitas.turno.adapter.out.feign;

import com.patitas.turno.domain.port.out.VeterinarioServicePort;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VeterinarioFeignAdapter implements VeterinarioServicePort {

    private final VeterinarioFeignClient client;

    @Override
    public boolean existsById(Long id) {
        try {
            client.findById(id);
            return true;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }
}
