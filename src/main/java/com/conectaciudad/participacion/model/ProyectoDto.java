package com.conectaciudad.participacion.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public record ProyectoDto(
        Long id,
        String nombre,
        String estado,
        LocalDateTime fechaInicio,
        LocalDateTime fechaFin
) implements Serializable {
}
