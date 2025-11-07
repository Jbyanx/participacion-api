package com.conectaciudad.participacion.service.client;

import com.conectaciudad.participacion.dto.ProyectoDto;
import com.conectaciudad.participacion.dto.UserDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(
        name = "proyectoClient",
        url = "${services.grupo2.base-url}", // se lee desde application.yml o .env
        path = "/api/v1"
)
public interface ProyectoClient {

    // Obtener proyecto por ID
    @GetMapping("/projects/{idProyecto}")
    ProyectoDto obtenerProyectoPorId(@PathVariable("idProyecto") Long idProyecto);

    // Listar todos los proyectos (solo admin)
    @GetMapping("/projects")
    List<ProyectoDto> obtenerProyectosPublicados();

    // Obtener ID de ciudadano por username
    @GetMapping("/users/{ciudadanoUsername}")
    UserDto obtenerCiudadanoPorUsername(@PathVariable("ciudadanoUsername") String ciudadanoUsername);
}
