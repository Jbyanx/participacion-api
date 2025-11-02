package com.conectaciudad.participacion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ProyectoDto(
        Long id,
        String name,
        String estado,
        String objectives,
        String beneficiaryPopulations,
        String budgets,
        LocalDateTime startAt,
        LocalDateTime endAt,
        Long creator, //creator user id
        ProjectStatus status
) implements Serializable {
}