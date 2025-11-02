package com.conectaciudad.participacion.service.client;

import com.conectaciudad.participacion.exception.ProyectoNotFoundException;
import com.conectaciudad.participacion.exception.ProyectoServiceNotAvilableException;
import com.conectaciudad.participacion.dto.ProyectoDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.util.List;

@Service
public class ProyectoClient {
    private final WebClient webClient;
    @Value("${services.grupo2.base-url}")
    private String proyectosUrl;

    public ProyectoClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public ProyectoDto obtenerProyectoPorId(Long idProyecto) {
        return webClient.get()
                .uri(proyectosUrl+"/api/v1/projects/"+idProyecto)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).map(ProyectoServiceNotAvilableException::new)
                )
                .bodyToMono(ProyectoDto.class)
                .block();
    }

    public List<ProyectoDto> obtenerProyectosPublicados() {
        return webClient.get()
                .uri(proyectosUrl+"/api/v1/projects")//solo ADMIN
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).map(ProyectoServiceNotAvilableException::new)
                )
                .bodyToFlux(ProyectoDto.class)
                .collectList()
                .block();
    }

    public Long obtenerCiudadanoPorUsername(String ciudadanoUsername) {
        return webClient.get()
                .uri(proyectosUrl+"/api/v1/users/"+ciudadanoUsername)
                .retrieve()
                .onStatus(HttpStatusCode::isError, response ->
                        response.bodyToMono(String.class).map(ProyectoServiceNotAvilableException::new)
                )
                .bodyToMono(ProyectoDto.class)
                .map(ProyectoDto::id)
                .block();
    }
}
