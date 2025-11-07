package com.conectaciudad.participacion.service.client;

import com.conectaciudad.participacion.dto.EstadoProyecto;
import com.conectaciudad.participacion.dto.ProyectoDto;
import com.conectaciudad.participacion.dto.RespuestaVotoDTO;
import com.conectaciudad.participacion.exception.VotoInvalidoException;
import com.conectaciudad.participacion.mapper.VotacionMapper;
import com.conectaciudad.participacion.model.Votacion;
import com.conectaciudad.participacion.repository.AuditoriaRepository;
import com.conectaciudad.participacion.repository.VotacionRepository;
import com.conectaciudad.participacion.service.impl.VotacionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
class VotacionServiceImplTest {

    @Mock
    private VotacionRepository votacionRepository;

    @Mock
    private AuditoriaRepository auditoriaRepository;

    @Mock
    private ProyectoClient proyectoClient;

    @Mock
    private VotacionMapper votacionMapper;

    @InjectMocks
    private VotacionServiceImpl votacionService;

    @Test
    void registrarVoto_exitoso() {
        // Mock Proyecto activo
        ProyectoDto proyectoMock = new ProyectoDto(
                1L, "Proyecto nuevo",
                "SALVAR AL MUNDO",
                "COLOMBIA Y SU PAISES FRONTERIZOS",
                "budgets",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1L,
                EstadoProyecto.APROBADO
        );

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyectoMock);
        when(votacionRepository.existsByProyectoIdAndCiudadanoId(1L, 100L)).thenReturn(false);

        Votacion votoGuardado = new Votacion();
        votoGuardado.setId(1L);
        votoGuardado.setDecision(true);

        when(votacionRepository.save(any(Votacion.class))).thenReturn(votoGuardado);

        RespuestaVotoDTO respuesta = votacionService.registrarVoto(1L, true, 100L);

        assertThat(respuesta).isNotNull();
        assertThat(respuesta.idVoto()).isEqualTo(1L);
        assertThat(respuesta.decision()).isTrue();
        assertThat(respuesta.mensaje()).contains("registrado");
    }

    @Test
    void registrarVoto_lanzaExcepcion_siProyectoFueraDeRango() {
        ProyectoDto proyectoExpirado = new ProyectoDto(
                1L, "Proyecto nuevo",
                "SALVAR AL MUNDO",
                "COLOMBIA Y SU PAISES FRONTERIZOS",
                "budgets",
                LocalDateTime.now().minusDays(1),
                LocalDateTime.now().plusDays(1),
                1L,
                EstadoProyecto.APROBADO
        );

        when(proyectoClient.obtenerProyectoPorId(1L)).thenReturn(proyectoExpirado);

        assertThatThrownBy(() -> votacionService.registrarVoto(1L, true, 123L))
                .isInstanceOf(VotoInvalidoException.class)
                .hasMessageContaining("finalizó");
    }
}
