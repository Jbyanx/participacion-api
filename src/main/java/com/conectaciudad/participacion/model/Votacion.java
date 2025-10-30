package com.conectaciudad.participacion.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "votaciones", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"ciudadano_id", "proyecto_id"})
})
public class Votacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false)
    private Boolean decision; // true = acuerdo, false = desacuerdo

    @Column(nullable = false, unique = true)
    private String hashVerificacion;

    @Column(name = "proyecto_id", nullable = false)
    private Long proyectoId; // Referencia externa al grupo 2

    @Column(name = "ciudadano_id", nullable = false)
    private Long ciudadanoId; // Referencia o usuario autenticado

    @OneToMany(mappedBy = "votacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AuditoriaVoto> auditorias = new ArrayList<>();

}
