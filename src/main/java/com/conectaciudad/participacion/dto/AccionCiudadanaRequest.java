package com.conectaciudad.participacion.dto;

public record AccionCiudadanaRequest(
        Long projectId,
        String actionType, // Swagger enum: "CITIZEN_VOTE"
        String description // Aquí mandaremos "A FAVOR" o "EN CONTRA"
) {}