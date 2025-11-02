package com.conectaciudad.participacion.dto;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public record VotoDetailDTO(
    Long id,
    LocalDateTime fechaHora,
    boolean decision,
    String hashVerificacion,
    Long proyectoId,
    Long ciudadanoId,
    List<Long> auditoriasId
) implements Serializable {
}
