package com.conectaciudad.participacion.repository;

import com.conectaciudad.participacion.model.AlertaAuditoria;
import com.conectaciudad.participacion.model.AuditoriaVoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlertaAuditoriaRepository extends JpaRepository<AlertaAuditoria, Long> {
}
