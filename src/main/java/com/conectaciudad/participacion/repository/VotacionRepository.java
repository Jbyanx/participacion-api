package com.conectaciudad.participacion.repository;

import com.conectaciudad.participacion.model.Votacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VotacionRepository extends JpaRepository<Votacion, Long> {
    // Buscar si un ciudadano ya votó por un proyecto
    Optional<Votacion> findByCiudadanoIdAndProyectoId(Long ciudadanoId, Long proyectoId);

    // Listar todos los votos de un proyecto (para resultados)
    List<Votacion> findByProyectoId(Long proyectoId);

    // Contar votos positivos y negativos (usado en estadísticas)
    long countByProyectoIdAndDecisionTrue(Long proyectoId);
    long countByProyectoIdAndDecisionFalse(Long proyectoId);
}
