package com.conectaciudad.participacion.service.client;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@RequiredArgsConstructor
public class AuthClient {

    private final Logger logger = LoggerFactory.getLogger(AuthClient.class);
    private final RestTemplate restTemplate = new RestTemplate();

    private static final String LOGIN_URL = "https://conecta-ciudad-api.azurewebsites.net/auth/login";

    public AuthLoginResponse login() {
        try {
            AuthLoginRequest request = new AuthLoginRequest(
                    "carlos.rodriguez@example.com",
                    "MiContraseñaSegura123!"
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<AuthLoginRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<AuthLoginResponse> response = restTemplate.exchange(
                    LOGIN_URL,
                    HttpMethod.POST,
                    entity,
                    AuthLoginResponse.class
            );

            logger.info("Login exitoso. Token recibido: {}", response.getBody().token());
            return response.getBody();

        } catch (Exception e) {
            logger.error("Error durante login con el servidor de autenticación: {}", e.getMessage());
            throw new RuntimeException("No se pudo conectar con el sistema de autenticación", e);
        }
    }
}
