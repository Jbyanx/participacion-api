package com.conectaciudad.participacion.service.impl;

import com.conectaciudad.participacion.dto.AlertaAuditoriaDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;
import com.conectaciudad.participacion.mapper.VotacionMapper;
import com.conectaciudad.participacion.model.AlertaAuditoria;
import com.conectaciudad.participacion.repository.AuditoriaRepository;
import com.conectaciudad.participacion.repository.VotacionRepository;
import com.conectaciudad.participacion.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final VotacionRepository votacionRepository;
    private final VotacionMapper votacionMapper;

    @Override
    public List<VotoDetailDTO> listarTodosLosVotos() {
        return votacionRepository.findAll().stream()
                .map(votacionMapper::toVotoDetail)
                .toList();
    }

    @Override
    public List<VotoDetailDTO> listarVotosPorProyecto(Long idProyecto) {
        return votacionRepository.findByProyectoId(idProyecto).stream()
                .map(votacionMapper::toVotoDetail)
                .toList();
    }

    @Override
    public AlertaAuditoriaDTO registrarAlerta(AlertaAuditoriaDTO alertaDTO) {
        AlertaAuditoria alerta = new AlertaAuditoria();
        alerta.setDescripcion(alertaDTO.descripcion());
        alerta.setIdProyecto(alertaDTO.idProyecto());
        alerta.setTipo(alertaDTO.tipo());
        alerta.setFechaRegistro(LocalDateTime.now());

        // Nuevos campos de robustez
        alerta.setSeveridad(alertaDTO.severidad() != null ? alertaDTO.severidad() : "MEDIA");
        alerta.setOrigen(alertaDTO.origen() != null ? alertaDTO.origen() : "VOTACION");
        alerta.setUsuario(alertaDTO.usuario());
        alerta.setAccion(alertaDTO.accion());
        alerta.setIpOrigen(alertaDTO.ipOrigen());
        alerta.setRevisada(false);

        auditoriaRepository.save(alerta);

        return new AlertaAuditoriaDTO(
                alerta.getId(),
                alerta.getIdProyecto(),
                alerta.getDescripcion(),
                alerta.getTipo(),
                alerta.getFechaRegistro().toString(),
                alerta.getSeveridad(),
                alerta.getOrigen(),
                alerta.getUsuario(),
                alerta.getAccion(),
                alerta.getIpOrigen(),
                alerta.isRevisada()
        );
    }

    @Override
    public List<AlertaAuditoriaDTO> listarAlertas() {
        return auditoriaRepository.findAll().stream()
                .map(a -> new AlertaAuditoriaDTO(
                        a.getId(),
                        a.getIdProyecto(),
                        a.getDescripcion(),
                        a.getTipo(),
                        a.getFechaRegistro().toString(),
                        a.getSeveridad(),
                        a.getOrigen(),
                        a.getUsuario(),
                        a.getAccion(),
                        a.getIpOrigen(),
                        a.isRevisada()
                ))
                .toList();
    }
}
