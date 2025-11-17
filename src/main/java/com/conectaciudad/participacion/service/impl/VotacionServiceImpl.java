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
        logger.debug("Registrando voto del ciudadano {}", ciudadanoId);

        // 1. Validar que el proyecto existe
        ProyectoDto proyecto;
        try {
            proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        } catch (Exception e) {
            logger.error("Proyecto {} no encontrado", idProyecto, e);
            throw new ProyectoNotFoundException("Proyecto no encontrado");
        }

        logger.debug("Proyecto {} encontrado con estado: {}", idProyecto, proyecto.status());

        // 2. Validar ventana de votación
        LocalDateTime ahora = LocalDateTime.now();

        if (ahora.isBefore(proyecto.startAt())) {
            throw new VotoInvalidoException("La votación aún no ha comenzado para este proyecto.");
        }

        if (ahora.isAfter(proyecto.endAt())) {
            throw new VotoInvalidoException("El periodo de votación ya finalizó para este proyecto.");
        }

        // 3. Verificar si el ciudadano ya votó ese proyecto
        boolean yaVoto = votacionRepository.existsByProyectoIdAndCiudadanoId(idProyecto, ciudadanoId);
        if (yaVoto) {
            throw new VotoDuplicadoException("El ciudadano ya emitió su voto para este proyecto");
        }

        // 4. Crear nueva votación
        Votacion voto = Votacion.builder()
                .ciudadanoId(ciudadanoId)
                .proyectoId(idProyecto)
                .decision(decision)
                .fechaHora(LocalDateTime.now())
                .build();

        // 5. Generar hash de integridad
        String raw = ciudadanoId + "|" + idProyecto + "|" + voto.getFechaHora() + "|" + decision;
        String hash = generarHash(raw);
        voto.setHashVerificacion(hash);

        // 6. Guardar voto
        Votacion votoGuardado = votacionRepository.save(voto);
        logger.info("Voto {} guardado exitosamente", votoGuardado.getId());

        // 7. Registrar auditoría
        AuditoriaVoto auditoria = new AuditoriaVoto();
        auditoria.setVotacion(votoGuardado);
        auditoria.setFechaEvento(LocalDateTime.now());
        auditoria.setTipoEvento("CREACION_VOTO");
        auditoria.setDescripcion("Voto registrado correctamente.");
        auditoria.setUsuarioResponsable(String.valueOf(ciudadanoId));
        auditoria.setHashNuevo(hash);
        auditoriaVotoRepository.save(auditoria);

        // 8. 🆕 NUEVO: Notificar al Grupo 2 sobre la acción de participación
        notificarAccionParticipacion(idProyecto, ciudadanoId, decision, voto.getFechaHora());

        // 9. Retornar respuesta
        return new RespuestaVotoDTO(
                votoGuardado.getId(),
                decision,
                "Voto registrado correctamente.",
                ciudadanoId
        );
    }

    /**
     * Notifica al sistema del Grupo 2 sobre la acción de participación.
     * No lanza excepción si falla, solo registra el error.
     */
    private void notificarAccionParticipacion(Long idProyecto, Long ciudadanoId, boolean decision, LocalDateTime fechaHora) {
        try {
            AccionCiudadanaRequest accion = new AccionCiudadanaRequest(
                    idProyecto,
                    ciudadanoId,
                    "VOTE",
                    decision ? "A_FAVOR" : "EN_CONTRA",
                    fechaHora
            );

            proyectoClient.registrarAccionCiudadana(accion);
            logger.info("Acción de participación notificada al Grupo 2 para proyecto {} y ciudadano {}",
                    idProyecto, ciudadanoId);

        } catch (Exception e) {
            // No fallar el voto si no se puede notificar al Grupo 2
            logger.warn("No se pudo notificar la acción de participación al Grupo 2. " +
                            "Proyecto: {}, Ciudadano: {}. Error: {}",
                    idProyecto, ciudadanoId, e.getMessage());
        }
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

    @Override
    public VotoDetailDTO obtenerVotoPorCiudadanoYProyecto(Long ciudadanoId, Long idProyecto) {
        Votacion votacion = votacionRepository.findByCiudadanoIdAndProyectoId(ciudadanoId, idProyecto)
                .orElseThrow(() -> new VotacionNotFoundException("no existe un voto para ese ciudadano y ese proyecto"));
        return votacionMapper.toVotoDetail(votacion);
    }
}