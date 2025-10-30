package com.conectaciudad.participacion.service;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ProyectoClient {
    private final WebClient webClient;

    public ProyectoClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public List<Object> obtenerProyectosPublicados() {
        return webClient.get()
                .uri("https://identidad-api.onrender.com/api/v1/proyectos")
                .retrieve()
                .bodyToFlux(Object.class)
                .collectList()
                .block();
    }
}
