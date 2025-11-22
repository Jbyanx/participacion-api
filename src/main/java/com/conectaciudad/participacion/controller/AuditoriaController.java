package com.conectaciudad.participacion.controller;

import com.conectaciudad.participacion.dto.AlertaAuditoriaDTO;
import com.conectaciudad.participacion.service.AuditoriaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    // Endpoint manual para que el auditor ejecute la revisión
    // En AuditoriaController.java

    @PostMapping("/verificar-integridad")
    public ResponseEntity<?> ejecutarVerificacion(
            HttpServletRequest request, // Inyectamos el Request para sacar la IP
            Authentication authentication // Inyectamos Auth para sacar el usuario
    ) {
        // 1. Obtener quién ejecuta la acción
        String usuarioAuditor = (authentication != null) ? authentication.getName() : "ANONIMO_O_SISTEMA";

        // 2. Obtener IP real (considerando proxys de Azure)
        String ipAuditor = request.getHeader("X-Forwarded-For");
        if (ipAuditor == null || ipAuditor.isEmpty()) {
            ipAuditor = request.getRemoteAddr();
        }

        // 3. Llamar al servicio con los datos
        List<AlertaAuditoriaDTO> fraudes = auditoriaService.verificarIntegridadDelSistema(usuarioAuditor, ipAuditor);

        if (fraudes.isEmpty()) {
            return ResponseEntity.ok("Sistema íntegro. Verificación realizada por: " + usuarioAuditor);
        } else {
            // 207 Multi-Status
            return ResponseEntity.status(200).body(fraudes);
        }
    }

    // Ver el historial de alertas detectadas
    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaAuditoriaDTO>> obtenerAlertas() {
        return ResponseEntity.ok(auditoriaService.listarAlertas());
    }

    // Endpoint para ver los votos
    @GetMapping("/votos")
    public ResponseEntity<?> listarVotos() {
        return ResponseEntity.ok(auditoriaService.listarTodosLosVotos());
    }
}