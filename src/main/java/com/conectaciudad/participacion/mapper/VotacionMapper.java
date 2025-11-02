package com.conectaciudad.participacion.mapper;

import com.conectaciudad.participacion.dto.GuardarVotoDTO;
import com.conectaciudad.participacion.dto.VotoDetailDTO;
import com.conectaciudad.participacion.model.Votacion;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface VotacionMapper {
    VotoDetailDTO toVotoDetail(Votacion voto);

    Votacion toEntity(GuardarVotoDTO guardar);
}
