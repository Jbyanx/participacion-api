package com.conectaciudad.participacion.service;

import com.conectaciudad.participacion.dto.RespuestaVotoDTO;
import com.conectaciudad.participacion.dto.ResultadoVotacionDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;

public interface VotacionService {
    VotoDetailDTO obtenerVoto(Long votacionId);
    RespuestaVotoDTO registrarVoto(Long idProyecto, boolean decision, Long ciudadanoId);
    ResultadoVotacionDTO obtenerResultadosPorProyecto(Long idProyecto);

    VotoDetailDTO obtenerVotoPorCiudadanoYProyecto(Long ciudadanoId, Long idProyecto);
}
