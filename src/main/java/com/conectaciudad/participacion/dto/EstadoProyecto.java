package com.conectaciudad.participacion.dto;

public enum EstadoProyecto {
    DRAFT,
    PENDING_REVIEW,
    IN_REVIEW,
    RETURNED_WITH_OBSERVATIONS,
    READY_TO_PUBLISH,
    PUBLISHED,       // <--- Importante
    OPEN_FOR_VOTING, // <--- CRÍTICO
    VOTING_CLOSED,   // <--- Importante
    REJECTED
}
