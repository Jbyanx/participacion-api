package com.conectaciudad.participacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "alertas_auditoria")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AlertaAuditoria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long idProyecto;
    private String tipo;           // INFO, ADVERTENCIA, ERROR
    private String descripcion;

    private LocalDateTime fechaRegistro;

    // 🔹 Campos adicionales para robustez
    private String severidad;      // ALTA, MEDIA, BAJA
    private String origen;         // API, VOTACION, PARTICIPACION, etc.
    private String usuario;        // responsable (opcional)
    private String accion;         // acción realizada (ej. CREAR_VOTO)
    private String ipOrigen;       // opcional
    private boolean revisada = false;
}

