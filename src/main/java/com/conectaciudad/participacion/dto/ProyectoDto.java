package com.conectaciudad.participacion.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.io.Serializable;
import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true) // Ignorar campos extra
public record ProyectoDto(
        Long id,
        String name,
        String description,
        String objectives,
        String beneficiaryPopulations,
        Double budget,
        LocalDate startAt,
        LocalDate endAt,

        LocalDate votingStartAt,
        LocalDate votingEndAt,

        String status, // Recibimos String para evitar crash por Enum
        CreatorDto creator // Swagger muestra un objeto, no un ID plano
) implements Serializable {

    // Sub-DTO para mapear el creador sin que explote
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CreatorDto(Long id, String name, String email) implements Serializable {}

    // Helper para saber si el proyecto está en estado votable
    public boolean isEstadoVotable() {
        return "OPEN_FOR_VOTING".equalsIgnoreCase(status) ||
                "PUBLISHED".equalsIgnoreCase(status);
    }
}