package com.conectaciudad.participacion.controller;

import com.conectaciudad.participacion.dto.GuardarVotoDTO;
import com.conectaciudad.participacion.dto.RespuestaVotoDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;
import com.conectaciudad.participacion.exception.CiudadanoNotFoundException;
import com.conectaciudad.participacion.service.client.ProyectoClient;
import com.conectaciudad.participacion.service.impl.VotacionServiceImpl;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/votaciones")
@RequiredArgsConstructor
public class VotacionController {
    private final ProyectoClient proyectoClient;
    private final VotacionServiceImpl votacionService;
    private final Logger logger = LoggerFactory.getLogger(VotacionController.class);

    @PostMapping("/{idProyecto}")
    public ResponseEntity<RespuestaVotoDTO> votar(
            @PathVariable Long idProyecto,
            @RequestBody GuardarVotoDTO votoRequest,
            Authentication auth,
            UriComponentsBuilder uriComponentsBuilder) {

        //Extraer ID del ciudadano del token
        String ciudadanoUsername = auth.getPrincipal().toString();

        Long ciudadanoId = proyectoClient.obtenerCiudadanoPorUsername(ciudadanoUsername)
                .describeConstable().orElseThrow(() -> new CiudadanoNotFoundException(
                        "El ciudadano con usuario " + ciudadanoUsername + " no se encuentra registrado"));


        //Registrar voto
        RespuestaVotoDTO respuesta = votacionService.registrarVoto(
                idProyecto,
                votoRequest.decision(),
                ciudadanoId
        );

        // Construir URI del recurso recién creado
        URI location = uriComponentsBuilder
                .path("/votaciones/{id}")
                .buildAndExpand(respuesta.idVoto())
                .toUri();
        // Responder con 201 Created y el cuerpo con información del voto
        return ResponseEntity.created(location).body(respuesta);
    }

    @GetMapping("/{votacionId}")
    public ResponseEntity<VotoDetailDTO> verVoto(@PathVariable Long votacionId){
        return ResponseEntity.ok(votacionService.obtenerVoto(votacionId));
    }

}
