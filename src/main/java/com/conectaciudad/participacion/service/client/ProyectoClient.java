package com.conectaciudad.participacion.service.client;

import com.conectaciudad.participacion.dto.AccionCiudadanaRequest;
import com.conectaciudad.participacion.dto.ProyectoDto;
import com.conectaciudad.participacion.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "proyectoClient",
        url = "${services.grupo2.base-url}",
        path = "/api/v1"
)
public interface ProyectoClient {

    @GetMapping("/projects/{idProyecto}")
    ProyectoDto obtenerProyectoPorId(@PathVariable("idProyecto") Long idProyecto);

    @GetMapping("/projects")
    List<ProyectoDto> obtenerProyectosPublicados();

    @GetMapping("/users/{ciudadanoUsername}")
    UserDto obtenerCiudadanoPorUsername(@PathVariable("ciudadanoUsername") String ciudadanoUsername);

    @PostMapping("/citizen-actions")
    void registrarAccionCiudadana(@RequestBody AccionCiudadanaRequest request);
}