package com.conectaciudad.participacion.service.impl;

import com.conectaciudad.participacion.dto.AlertaAuditoriaDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;
import com.conectaciudad.participacion.mapper.VotacionMapper;
import com.conectaciudad.participacion.model.AlertaAuditoria;
import com.conectaciudad.participacion.model.Votacion;
import com.conectaciudad.participacion.repository.AlertaAuditoriaRepository;
import com.conectaciudad.participacion.repository.AuditoriaVotoRepository;
import com.conectaciudad.participacion.repository.VotacionRepository;
import com.conectaciudad.participacion.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuditoriaServiceImpl implements AuditoriaService {

    private final AuditoriaVotoRepository auditoriaVotoRepository;
    private final AlertaAuditoriaRepository alertaAuditoriaRepository;
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

        alertaAuditoriaRepository.save(alerta);

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
        return alertaAuditoriaRepository.findAll().stream()
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

    @Override
    @Transactional
    public List<AlertaAuditoriaDTO> verificarIntegridadDelSistema(String usuarioAuditor, String ipAuditor) {
        List<Votacion> todosLosVotos = votacionRepository.findAll();
        List<AlertaAuditoriaDTO> alertasGeneradas = new ArrayList<>();

        for (Votacion voto : todosLosVotos) {
            // 1. Reconstruir el string original EXACTAMENTE igual que al crear el voto
            String rawData = voto.getCiudadanoId() + "|" +
                    voto.getProyectoId() + "|" +
                    voto.getFechaHora() + "|" +
                    voto.getDecision();

            // 2. Recalcular el hash
            String hashCalculado = generarHash(rawData);

            // 3. Comparar con el que está en base de datos
            if (!hashCalculado.equals(voto.getHashVerificacion())) {

                // ¡ALERTA! Alguien tocó la base de datos manualmente
                AlertaAuditoria alerta = new AlertaAuditoria();
                alerta.setIdProyecto(voto.getProyectoId());
                alerta.setTipo("FRAUDE_DETECTADO");
                alerta.setUsuario(usuarioAuditor); // "Admin" o el email del token
                alerta.setIpOrigen(ipAuditor);     // IP desde donde se pidió verificar
                alerta.setSeveridad("CRITICA");
                alerta.setOrigen("MOTOR_AUDITORIA");
                alerta.setFechaRegistro(LocalDateTime.now());
                alerta.setDescripcion("INTEGRIDAD VIOLADA en Voto ID: " + voto.getId() +
                        ". Hash en BD: " + voto.getHashVerificacion() +
                        " vs Calculado: " + hashCalculado);
                alerta.setAccion("VERIFICACION_HASH");
                alerta.setRevisada(false);

                alertaAuditoriaRepository.save(alerta);

                // Agregamos a la respuesta para que el auditor lo vea inmediatamente
                alertasGeneradas.add(mapToDTO(alerta));
            }
        }

        return alertasGeneradas;
    }

    private String generarHash(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error crítico hashing", e);
        }
    }

    private AlertaAuditoriaDTO mapToDTO(AlertaAuditoria a) {
        return new AlertaAuditoriaDTO(
                a.getId(), a.getIdProyecto(), a.getDescripcion(), a.getTipo(),
                a.getFechaRegistro().toString(), a.getSeveridad(), a.getOrigen(),
                a.getUsuario(), a.getAccion(), a.getIpOrigen(), a.isRevisada()
        );
    }
}
