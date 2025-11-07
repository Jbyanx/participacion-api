package com.conectaciudad.participacion.dto;

import java.io.Serializable;

public record AlertaAuditoriaDTO(
        Long id,
        Long idProyecto,
        String descripcion,
        String tipo,
        String fechaRegistro,
        String severidad,
        String origen,
        String usuario,
        String accion,
        String ipOrigen,
        boolean revisada
) implements Serializable {}
