package com.conectaciudad.participacion.service;

import com.conectaciudad.participacion.dto.RespuestaVotoDTO;
import com.conectaciudad.participacion.dto.ResultadoVotacionDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;
import org.springframework.security.core.Authentication;

public interface VotacionService {
    VotoDetailDTO obtenerVoto(Long votacionId);
    RespuestaVotoDTO registrarVoto(Long idProyecto, boolean decision, Authentication authentication);
    ResultadoVotacionDTO obtenerResultadosPorProyecto(Long idProyecto);

    VotoDetailDTO obtenerVotoPorCiudadanoYProyecto(Authentication authentication, Long ciudadanoId);
}
