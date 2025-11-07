package com.conectaciudad.participacion.service.client;

import java.io.Serializable;

public record AuthLoginRequest(
        String email,
        String password
) implements Serializable {
}
