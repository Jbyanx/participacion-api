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

    private LocalDateTime fechaHora;

    private String severidad; // ALTA, MEDIA, BAJA
    private String mensaje;
    private String origen; // API, BD, USUARIO, etc.
}

