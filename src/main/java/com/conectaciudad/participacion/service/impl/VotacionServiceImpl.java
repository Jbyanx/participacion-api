package com.conectaciudad.participacion.service.impl;

import com.conectaciudad.participacion.dto.*;
import com.conectaciudad.participacion.exception.*;
import com.conectaciudad.participacion.mapper.VotacionMapper;
import com.conectaciudad.participacion.model.AuditoriaVoto;
import com.conectaciudad.participacion.model.Votacion;
import com.conectaciudad.participacion.repository.AuditoriaVotoRepository;
import com.conectaciudad.participacion.repository.VotacionRepository;
import com.conectaciudad.participacion.service.VotacionService;
import com.conectaciudad.participacion.service.client.ProyectoClient;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VotacionServiceImpl implements VotacionService {
    private final VotacionRepository votacionRepository;
    private final VotacionMapper votacionMapper;
    private final ProyectoClient proyectoClient;
    private final AuditoriaVotoRepository auditoriaVotoRepository;
    private final Logger logger = LoggerFactory.getLogger(VotacionServiceImpl.class);

    public VotoDetailDTO obtenerVoto(Long votacionId) {
        return votacionRepository.findById(votacionId)
                .map(votacionMapper::toVotoDetail)
                .orElseThrow(() -> new VotacionNotFoundException("La votacion id: "+votacionId+" no está registrada"));
    }

    @Override
    @Transactional
    public RespuestaVotoDTO registrarVoto(Long idProyecto, boolean decision, Long ciudadanoId) {
        logger.debug("Registrando voto de la ciudadano");

        ProyectoDto proyecto;
        try {
            proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        } catch (Exception e) {
            logger.error("proyecto no encontrado");
            throw new ProyectoNotFoundException("proyecto no encontrado");
        }

        logger.debug("proyecto encontrado");
        LocalDateTime ahora = LocalDateTime.now();


        if (ahora.isBefore(proyecto.startAt())) {
            throw new VotoInvalidoException("La votación aún no ha comenzado para este proyecto.");
        }

        if (ahora.isAfter(proyecto.endAt())) {
            throw new VotoInvalidoException("El periodo de votación ya finalizó para este proyecto.");
        }

        // Verificar si el ciudadano ya votó ese proyecto
        boolean yaVoto = votacionRepository.existsByProyectoIdAndCiudadanoId(idProyecto, ciudadanoId);
        if (yaVoto) {
            throw new VotoDuplicadoException("El ciudadano ya emitió su voto para este proyecto");
        }

        // Crear nueva votación
        Votacion voto = Votacion.builder()
                .ciudadanoId(ciudadanoId)
                .proyectoId(idProyecto)
                .decision(decision)
                .fechaHora(LocalDateTime.now())
                .build();

        // Generar hash de integridad
        String raw = ciudadanoId + "|" + idProyecto + "|" + voto.getFechaHora() + "|" + decision;
        String hash = generarHash(raw);
        voto.setHashVerificacion(hash);

        // Guardar voto
        Votacion votoGuardado = votacionRepository.save(voto);

        // Registrar auditoría
        AuditoriaVoto auditoria = new AuditoriaVoto();
        auditoria.setVotacion(votoGuardado);
        auditoria.setFechaEvento(LocalDateTime.now());
        auditoria.setTipoEvento("CREACION_VOTO");
        auditoria.setDescripcion("Voto registrado correctamente.");
        auditoria.setUsuarioResponsable(String.valueOf(ciudadanoId));
        auditoria.setHashNuevo(hash);

        auditoriaVotoRepository.save(auditoria);

        // Retornar respuesta
        return new RespuestaVotoDTO(
                votoGuardado.getId(),
                decision,
                "Voto registrado correctamente.",
                ciudadanoId
        );
    }

    private String generarHash(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Error generando hash de verificación", e);
        }
    }

    @Override
    public ResultadoVotacionDTO obtenerResultadosPorProyecto(Long idProyecto) {
        ProyectoDto proyecto = proyectoClient.obtenerProyectoPorId(idProyecto); //lanza excepcion si no existe

        if (!proyecto.status().equals(EstadoProyecto.PUBLICADO) &&
                !proyecto.status().equals(EstadoProyecto.APROBADO) &&
                !proyecto.status().equals(EstadoProyecto.LISTO_PARA_PUBLICAR)) {
            throw new VotoInvalidoException(
                    "No se pueden consultar resultados para un proyecto en estado: " + proyecto.status()
            );
        }

        long votosAFavor = votacionRepository.countByProyectoIdAndDecisionTrue(idProyecto);
        long votosEnContra = votacionRepository.countByProyectoIdAndDecisionFalse(idProyecto);
        long total = votosAFavor + votosEnContra;

        double porcentajeAFavor = total > 0 ? (votosAFavor * 100.0 / total) : 0;
        double porcentajeEnContra = total > 0 ? (votosEnContra * 100.0 / total) : 0;

        return new ResultadoVotacionDTO(
                idProyecto,
                votosAFavor,
                votosEnContra,
                porcentajeAFavor,
                porcentajeEnContra
        );
    }

}