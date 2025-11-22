package com.conectaciudad.participacion.exception.security;

import com.conectaciudad.participacion.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException, ServletException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiError apiError = new ApiError(
                LocalDateTime.now(),
                401,
                "Unauthorized",
                "No Autorizado: Debes iniciar sesión (Token faltante o inválido).",
                request.getRequestURI()
        );

        // Usamos Jackson para escribir el JSON manualmente en la respuesta
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule()); // Para que LocalDateTime se vea bien
        mapper.writeValue(response.getOutputStream(), apiError);
    }
}
