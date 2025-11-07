package com.conectaciudad.participacion.controller;

import com.conectaciudad.participacion.dto.AlertaAuditoriaDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;
import com.conectaciudad.participacion.service.AuditoriaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/auditorias")
@RequiredArgsConstructor
public class AuditoriaController {

    private final AuditoriaService auditoriaService;

    @GetMapping("/votos")
    public ResponseEntity<List<VotoDetailDTO>> listarVotos() {
        return ResponseEntity.ok(auditoriaService.listarTodosLosVotos());
    }

    @GetMapping("/votos/{idProyecto}")
    public ResponseEntity<List<VotoDetailDTO>> listarVotosPorProyecto(@PathVariable Long idProyecto) {
        return ResponseEntity.ok(auditoriaService.listarVotosPorProyecto(idProyecto));
    }

    @PostMapping("/alertas")
    public ResponseEntity<AlertaAuditoriaDTO> registrarAlerta(@RequestBody AlertaAuditoriaDTO alertaDTO) {
        return ResponseEntity.ok(auditoriaService.registrarAlerta(alertaDTO));
    }

    @GetMapping("/alertas")
    public ResponseEntity<List<AlertaAuditoriaDTO>> obtenerAlertas() {
        return ResponseEntity.ok(auditoriaService.listarAlertas());
    }
}