package com.conectaciudad.participacion.model;

import jakarta.persistence.*;
import lombok.*;

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

    @Column(name = "id_proyecto") // Mapeo explícito
    private Long idProyecto;

    private String tipo;
    private String descripcion;

    @Column(name = "fecha_registro") // Mapeo de fecha_hora a fecha_registro
    private LocalDateTime fechaRegistro;

    // changeSet 4
    private String severidad;
    private String origen;
    private String usuario;
    private String accion;

    @Column(name = "ip_origen") // Mapeo explícito
    private String ipOrigen;

    private boolean revisada = false;
}