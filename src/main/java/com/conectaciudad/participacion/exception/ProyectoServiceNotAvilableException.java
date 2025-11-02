package com.conectaciudad.participacion.exception;

public class ProyectoServiceNotAvilableException extends RuntimeException {
    public ProyectoServiceNotAvilableException(String message) {
        super(message);
    }

    public ProyectoServiceNotAvilableException(String message, Throwable cause) {
        super(message, cause);
    }
}
