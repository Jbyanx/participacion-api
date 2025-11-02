package com.conectaciudad.participacion.repository;

import com.conectaciudad.participacion.model.AuditoriaVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaVotoRepository extends JpaRepository<AuditoriaVoto, Long> {
    // Listar los eventos de auditoría de un voto
    List<AuditoriaVoto> findByVotacionId(Long votacionId);

    // Buscar por tipo de evento (por ejemplo, "MODIFICACION", "CREACION")
    List<AuditoriaVoto> findByTipoEvento(String tipoEvento);

    // Buscar por fecha
    List<AuditoriaVoto> findByFechaEventoBetween(LocalDateTime inicio, LocalDateTime fin);
}
