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
import com.conectaciudad.participacion.service.jwt.JwtService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class VotacionServiceImpl implements VotacionService {

    private final VotacionRepository votacionRepository;
    private final VotacionMapper votacionMapper;
    private final ProyectoClient proyectoClient;
    private final AuditoriaVotoRepository auditoriaVotoRepository;
    private final JwtService jwtService; // Inyectamos JwtService
    private final Logger logger = LoggerFactory.getLogger(VotacionServiceImpl.class);

    @Override
    @Transactional
    public RespuestaVotoDTO registrarVoto(Long idProyecto, boolean decision, Authentication authentication) {

        // 1. OBTENER IDENTIDAD (Sin llamar a API externa, usando el token)
        String token = (String) authentication.getCredentials();
        Long ciudadanoId = jwtService.extractUserId(token);

        logger.info("Iniciando voto. CiudadanoID: {}, ProyectoID: {}", ciudadanoId, idProyecto);

        // 2. OBTENER PROYECTO (Llamada al Grupo 2)
        ProyectoDto proyecto;
        try {
            proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);
        } catch (Exception e) {
            logger.error("Error consultando proyecto {}: {}", idProyecto, e.getMessage());
            throw new ProyectoNotFoundException("El proyecto no existe o el servicio no responde.");
        }

        // 3. VALIDACIÓN DE ESTADO (Reglas de Negocio según Swagger)
        // Aceptamos "OPEN_FOR_VOTING" (estado explícito) o "PUBLISHED" (por si acaso)
        boolean esEstadoValido = "OPEN_FOR_VOTING".equalsIgnoreCase(proyecto.status()) ||
                "PUBLISHED".equalsIgnoreCase(proyecto.status());

        if (!esEstadoValido) {
            throw new VotoInvalidoException("El proyecto no está habilitado para votación (Estado: " + proyecto.status() + ")");
        }

        // 4. VALIDACIÓN DE FECHAS (Usando votingStartAt y votingEndAt del Swagger)
        ZoneId bogotaZone = ZoneId.of("America/Bogota");
        LocalDateTime ahora = LocalDateTime.now(bogotaZone);

        // Convertimos LocalDate a rangos de tiempo precisos
        // Si votingStartAt es null, asumimos que NO ha empezado (seguridad)
        LocalDateTime inicioUrnas = (proyecto.votingStartAt() != null)
                ? proyecto.votingStartAt().atStartOfDay()
                : LocalDateTime.MAX;

        // Si votingEndAt es null, asumimos que NO termina (o validamos contra endAt del proyecto)
        LocalDateTime cierreUrnas = (proyecto.votingEndAt() != null)
                ? proyecto.votingEndAt().atTime(23, 59, 59)
                : LocalDateTime.MIN;

        if (ahora.isBefore(inicioUrnas)) {
            throw new VotoInvalidoException("La votación abre el: " + proyecto.votingStartAt());
        }

        if (ahora.isAfter(cierreUrnas)) {
            throw new VotoInvalidoException("La votación cerró el: " + proyecto.votingEndAt());
        }

        // 5. CONTROL DE UNICIDAD (Un ciudadano, un voto)
        boolean yaVoto = votacionRepository.existsByProyectoIdAndCiudadanoId(idProyecto, ciudadanoId);
        if (yaVoto) {
            throw new VotoDuplicadoException("El ciudadano ya registró su participación en este proyecto.");
        }

        // 6. CREACIÓN DEL VOTO
        Votacion voto = Votacion.builder()
                .ciudadanoId(ciudadanoId)
                .proyectoId(idProyecto)
                .decision(decision)
                .fechaHora(ahora)
                .build();

        // 7. INTEGRIDAD (Hash SHA-256)
        // Garantiza que si cambian 'decision' en BD, el hash no coincidirá
        String rawData = ciudadanoId + "|" + idProyecto + "|" + voto.getFechaHora() + "|" + decision;
        String hash = generarHash(rawData);
        voto.setHashVerificacion(hash);

        // 8. GUARDAR
        Votacion votoGuardado = votacionRepository.save(voto);
        logger.info("Voto {} guardado exitosamente.", votoGuardado.getId());

        // 9. AUDITORÍA INTERNA (Trazabilidad Grupo 6)
        AuditoriaVoto auditoria = new AuditoriaVoto();
        auditoria.setVotacion(votoGuardado);
        auditoria.setFechaEvento(LocalDateTime.now(bogotaZone));
        auditoria.setTipoEvento("CREACION_VOTO");
        auditoria.setDescripcion("Participación registrada correctamente.");
        auditoria.setUsuarioResponsable(ciudadanoId.toString());
        auditoria.setHashNuevo(hash);
        auditoriaVotoRepository.save(auditoria);

        // 10. NOTIFICACIÓN EXTERNA (Al Grupo 2)
        notificarAccionParticipacion(idProyecto, decision);

        return new RespuestaVotoDTO(
                votoGuardado.getId(),
                decision,
                "Voto registrado correctamente.",
                ciudadanoId
        );
    }

    // --- Helper de Notificación ajustado al Swagger del Grupo 2 ---
    private void notificarAccionParticipacion(Long idProyecto, boolean decision) {
        try {
            String valorVoto = decision ? "A FAVOR" : "EN CONTRA";

            // Usamos la estructura exacta que del Swagger del grupo 2:
            // Solo pide projectId, actionType y description.
            AccionCiudadanaRequest accion = new AccionCiudadanaRequest(
                    idProyecto,
                    "CITIZEN_VOTE", // Enum estricto
                    "Voto registrado: " + valorVoto // Descripción
            );

            proyectoClient.registrarAccionCiudadana(accion);
            logger.debug("Notificación de participación enviada al Grupo 2");

        } catch (Exception e) {
            // IMPORTANTE: No fallar la transacción si la notificación falla.
            // El voto ya está guardado y es válido.
            logger.warn("No se pudo notificar al Grupo 2 (No bloqueante): {}", e.getMessage());
        }
    }

    // --- Helper de Hash ---
    private String generarHash(String contenido) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(contenido.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error crítico de seguridad: SHA-256 no disponible", e);
        }
    }
    // ... Resto de métodos (obtenerVoto, obtenerResultados, etc.) sin cambios ...

    @Override
    public VotoDetailDTO obtenerVoto(Long votacionId) {
        return votacionRepository.findById(votacionId)
                .map(votacionMapper::toVotoDetail)
                .orElseThrow(() -> new VotacionNotFoundException("La votacion id: "+votacionId+" no está registrada"));
    }

    @Override
    public ResultadoVotacionDTO obtenerResultadosPorProyecto(Long idProyecto) {
        // 1. Obtener info del proyecto
        ProyectoDto proyecto = proyectoClient.obtenerProyectoPorId(idProyecto);

        // 2. Validar si es público
        // Usamos String para evitar problemas con Enums del otro grupo
        String estado = proyecto.status();

        boolean esPublico = "PUBLISHED".equalsIgnoreCase(estado) ||
                "OPEN_FOR_VOTING".equalsIgnoreCase(estado) ||
                "VOTING_CLOSED".equalsIgnoreCase(estado);

        if (!esPublico) {
            // ¡AQUÍ LANZAMOS LA EXCEPCIÓN!
            throw new ProyectoNotAvailableException(
                    "Los resultados no están disponibles públicamente. El proyecto se encuentra en estado: " + estado
            );
        }

        // 3. Conteo de votos (EXCLUYENDO FRAUDULENTOS)
        long votosAFavor = votacionRepository.countByProyectoIdAndDecisionTrueAndFraudulentoFalse(idProyecto);
        long votosEnContra = votacionRepository.countByProyectoIdAndDecisionFalseAndFraudulentoFalse(idProyecto);

        long total = votosAFavor + votosEnContra;

        // 4. Cálculo de porcentajes
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

    public VotoDetailDTO obtenerVotoPorCiudadanoYProyecto(Authentication auth, Long idProyecto) {
        String token = auth.getCredentials().toString();
        Long ciudadanoId = jwtService.extractUserId(token);

        logger.info("Ciudadano ID: ciudadanoId: {}, token {}", ciudadanoId, token);

        Votacion votacion = votacionRepository.findByCiudadanoIdAndProyectoId(ciudadanoId, idProyecto)
                .orElseThrow(() -> new VotacionNotFoundException("No existe un voto para este ciudadano en este proyecto"));
        return votacionMapper.toVotoDetail(votacion);
    }
}