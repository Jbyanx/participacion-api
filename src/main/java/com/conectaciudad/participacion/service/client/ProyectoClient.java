package com.conectaciudad.participacion.service.client;

import com.conectaciudad.participacion.config.security.FeignConfig;
import com.conectaciudad.participacion.dto.AccionCiudadanaRequest;
import com.conectaciudad.participacion.dto.ProyectoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "proyectoClient",
        url = "https://conecta-ciudad-api.azurewebsites.net",
        path = "/api/v1",
        configuration = FeignConfig.class
)
public interface ProyectoClient {

    @GetMapping("/projects/{id}")
    ProyectoDto obtenerProyectoPorId(@PathVariable("id") Long id);

    @PostMapping("/citizen-actions")
    void registrarAccionCiudadana(@RequestBody AccionCiudadanaRequest request);
}