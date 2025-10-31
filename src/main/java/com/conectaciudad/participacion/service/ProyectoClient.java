package com.conectaciudad.participacion.service;

import com.conectaciudad.participacion.model.ProyectoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class ProyectoClient {
    private final WebClient webClient;
    private final String proyectosUrl;

    public ProyectoClient(WebClient webClient, @Value("${services.grupo2.base-url}/proyectos/publicados")
    String proyectosUrl) {
        this.webClient = webClient;
        this.proyectosUrl = proyectosUrl;
    }

    public List<ProyectoDto> obtenerProyectosPublicados() {
        return webClient.get()
                .uri(proyectosUrl)
                .retrieve()
                .bodyToFlux(ProyectoDto.class)
                .collectList()
                .block();
    }
}
