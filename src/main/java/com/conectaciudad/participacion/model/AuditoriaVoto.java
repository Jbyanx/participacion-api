package com.conectaciudad.participacion.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria_votos")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AuditoriaVoto {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "votacion_id", nullable = false)
    private Votacion votacion;

    @Column(nullable = false)
    private LocalDateTime fechaEvento;

    @Column(nullable = false)
    private String tipoEvento; // CREACION, MODIFICACION, FRAUDE, etc.

    @Column(length = 500)
    private String descripcion;

    @Column(name = "usuario_responsable")
    private String usuarioResponsable;

    @Column(name = "hash_anterior")
    private String hashAnterior;

    @Column(name = "hash_nuevo")
    private String hashNuevo;
}

