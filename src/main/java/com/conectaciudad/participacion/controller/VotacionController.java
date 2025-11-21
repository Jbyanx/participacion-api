package com.conectaciudad.participacion.controller;

import com.conectaciudad.participacion.dto.GuardarVotoDTO;
import com.conectaciudad.participacion.dto.RespuestaVotoDTO;
import com.conectaciudad.participacion.dto.ResultadoVotacionDTO;
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
            Authentication authentication,
            UriComponentsBuilder uriComponentsBuilder) {

        RespuestaVotoDTO respuesta = votacionService.registrarVoto(
                idProyecto,
                votoRequest.decision(),
                authentication
        );

        URI location = uriComponentsBuilder
                .path("/votaciones/{id}")
                .buildAndExpand(respuesta.idVoto())
                .toUri();

        return ResponseEntity.created(location).body(respuesta);
    }

    @GetMapping("/{votacionId}")
    public ResponseEntity<VotoDetailDTO> verVoto(@PathVariable Long votacionId){
        return ResponseEntity.ok(votacionService.obtenerVoto(votacionId));
    }

    @GetMapping("/{idProyecto}/resultados")
    public ResponseEntity<ResultadoVotacionDTO> obtenerResultados(@PathVariable Long idProyecto) {
        ResultadoVotacionDTO resultado = votacionService.obtenerResultadosPorProyecto(idProyecto);
        return ResponseEntity.ok(resultado);
    }

    @GetMapping("/{idProyecto}/mis-votos")
    public ResponseEntity<VotoDetailDTO> obtenerMiVoto(@PathVariable Long idProyecto, Authentication auth) {
        return ResponseEntity.ok(votacionService.obtenerVotoPorCiudadanoYProyecto(auth, idProyecto));
    }
}
