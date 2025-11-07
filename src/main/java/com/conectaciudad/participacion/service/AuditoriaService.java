package com.conectaciudad.participacion.service;

import com.conectaciudad.participacion.dto.AlertaAuditoriaDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;

import java.util.List;

public interface AuditoriaService {
    List<VotoDetailDTO> listarTodosLosVotos();
    List<VotoDetailDTO> listarVotosPorProyecto(Long idProyecto);
    AlertaAuditoriaDTO registrarAlerta(AlertaAuditoriaDTO alertaDTO);
    List<AlertaAuditoriaDTO> listarAlertas();
}
