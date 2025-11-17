package com.conectaciudad.participacion.dto;

import java.time.LocalDateTime;

public record AccionCiudadanaRequest(
        Long projectId,
        Long userId,
        String actionType, // "VOTE", "COMMENT", etc.
        String actionValue, // "A_FAVOR", "EN_CONTRA"
        LocalDateTime timestamp
) {}