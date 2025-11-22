package com.conectaciudad.participacion.exception;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            HttpServletRequest request
    ) {
        ApiError error = new ApiError(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(), // Ej: "Forbidden", "Unauthorized"
                message,
                request.getRequestURI()
        );
        return ResponseEntity.status(status).body(error);
    }

    // --------------------------
    // SEGURIDAD
    // --------------------------

    // ERROR 403: Usuario logueado pero sin rol para la accion (Ej: Ciudadano intentando auditar)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> handleAccessDeniedException(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.FORBIDDEN,
                "Acceso Denegado: No tienes los permisos necesarios (Rol insuficiente) para realizar esta acción.",
                request
        );
    }

    // ERROR 401: Token inválido, expirado o faltante
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiError> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return buildError(
                HttpStatus.UNAUTHORIZED,
                "No Autorizado: Debes iniciar sesión para acceder a este recurso.",
                request
        );
    }

    // --------------------------
    // EXCEPCIONES PERSONALIZADAS
    // --------------------------

    @ExceptionHandler(CiudadanoNotFoundException.class)
    public ResponseEntity<ApiError> handleCiudadanoNotFound(
            CiudadanoNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ProyectoNotFoundException.class)
    public ResponseEntity<ApiError> handleProyectoNotFound(
            ProyectoNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(ProyectoNotAvailableException.class)
    public ResponseEntity<ApiError> handleProyectoNotAvailable(
            ProyectoNotAvailableException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(ProyectoServiceNotAvilableException.class)
    public ResponseEntity<ApiError> handleProyectoServiceNotAvailable(
            ProyectoServiceNotAvilableException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.SERVICE_UNAVAILABLE, ex.getMessage(), request);
    }

    @ExceptionHandler(VotacionNotFoundException.class)
    public ResponseEntity<ApiError> handleVotacionNotFound(
            VotacionNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(VotoDuplicadoException.class)
    public ResponseEntity<ApiError> handleVotoDuplicado(
            VotoDuplicadoException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    @ExceptionHandler(VotoInvalidoException.class)
    public ResponseEntity<ApiError> handleVotoInvalido(
            VotoInvalidoException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    // --------------------------
    // EXCEPCIONES COMUNES
    // --------------------------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationErrors(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        String message = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(err -> err.getField() + ": " + err.getDefaultMessage())
                .findFirst()
                .orElse("Invalid request");

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> handleMethodNotSupported(
            HttpRequestMethodNotSupportedException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage(), request);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleInvalidBody(
            HttpMessageNotReadableException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.BAD_REQUEST, "Invalid or malformed JSON", request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> handleConstraintViolation(
            ConstraintViolationException ex,
            HttpServletRequest request
    ) {
        String message = ex.getConstraintViolations()
                .stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .findFirst()
                .orElse("Validation error");

        return buildError(HttpStatus.BAD_REQUEST, message, request);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> handleEntityNotFound(
            EntityNotFoundException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // --------------------------
    // FALLBACK
    // --------------------------

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiError> handleRuntime(
            RuntimeException ex,
            HttpServletRequest request
    ) {
        return buildError(HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected error: " + ex.getMessage(),
                request
        );
    }
}