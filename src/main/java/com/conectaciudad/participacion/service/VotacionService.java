package com.conectaciudad.participacion.service;

import com.conectaciudad.participacion.dto.RespuestaVotoDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;

public interface VotacionService {
    VotoDetailDTO obtenerVoto(Long votacionId);
    RespuestaVotoDTO registrarVoto(Long idProyecto, boolean decision, Long ciudadanoId);
}
