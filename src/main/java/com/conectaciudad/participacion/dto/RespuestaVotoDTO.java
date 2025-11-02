package com.conectaciudad.participacion.dto;

import java.io.Serializable;

public record RespuestaVotoDTO(
        Long idVoto, boolean decision, String mensaje, Long idVotante
) implements Serializable {
}
