package com.conectaciudad.participacion.dto;

import java.io.Serializable;

public record ResultadoVotacionDTO(
        Long idProyecto,
        long votosAFavor,
        long votosEnContra,
        double porcentajeAFavor,
        double porcentajeEnContra
) implements Serializable {
}
