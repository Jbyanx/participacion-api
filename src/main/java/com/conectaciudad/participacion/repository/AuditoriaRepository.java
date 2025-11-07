package com.conectaciudad.participacion.repository;

import com.conectaciudad.participacion.model.AlertaAuditoria;
import com.conectaciudad.participacion.model.AuditoriaVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditoriaRepository extends JpaRepository<AlertaAuditoria, Long> {
}
