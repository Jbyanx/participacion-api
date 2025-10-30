package com.conectaciudad.participacion.config;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class ActiveProfileLogger implements CommandLineRunner {

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public void run(String... args) {
        log.info("Spring active profile: "+ activeProfile);
    }
}
