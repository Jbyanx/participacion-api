package com.conectaciudad.participacion.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ActiveProfileLogger implements CommandLineRunner {
    Logger logger = LoggerFactory.getLogger(ActiveProfileLogger.class);

    @Value("${spring.profiles.active:default}")
    private String activeProfile;

    @Override
    public void run(String... args) {
        logger.info("Spring active profile: {}", activeProfile);
    }
}
