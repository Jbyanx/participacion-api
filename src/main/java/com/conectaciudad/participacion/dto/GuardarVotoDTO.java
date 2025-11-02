package com.conectaciudad.participacion.dto;

import java.io.Serializable;

public record GuardarVotoDTO(
        boolean decision
) implements Serializable {
}
