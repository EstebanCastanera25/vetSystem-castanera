package com.patitas.turno;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class MsTurnoApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsTurnoApplication.class, args);
    }
}
