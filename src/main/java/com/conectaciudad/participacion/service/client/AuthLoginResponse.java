package com.conectaciudad.participacion.service.client;

import java.io.Serializable;

public record AuthLoginResponse(
        String id,
        String message,
        String token,
        String username
) implements Serializable {
}
