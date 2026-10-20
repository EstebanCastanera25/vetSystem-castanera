package com.patitas.turno.adapter.out.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// el nombre tiene que ser igual al spring.application.name de ms-mascota (como aparece en eureka)
@FeignClient(name = "ms-mascota")
public interface MascotaFeignClient {

    @GetMapping(value = "/api/mascotas/{id}", produces = "application/json")
    MascotaResponse findById(@PathVariable("id") Long id);
}
