package com.conectaciudad.participacion.repository;

import com.conectaciudad.participacion.model.AlertaAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertaAuditoriaRepository extends JpaRepository<AlertaAuditoria, Long> {
    // Buscar por severidad (INFO, WARN, ERROR)
    List<AlertaAuditoria> findBySeveridad(String severidad);

    // Buscar por origen (por ejemplo, "Sistema", "Usuario", etc.)
    List<AlertaAuditoria> findByOrigen(String origen);

    // Buscar por fecha descendente
    List<AlertaAuditoria> findAllByOrderByFechaHoraDesc();
}
