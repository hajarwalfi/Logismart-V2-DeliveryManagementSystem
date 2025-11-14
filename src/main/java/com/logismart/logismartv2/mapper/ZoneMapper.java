package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.zone.ZoneCreateDTO;
import com.logismart.logismartv2.dto.zone.ZoneResponseDTO;
import com.logismart.logismartv2.dto.zone.ZoneUpdateDTO;
import com.logismart.logismartv2.entity.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ZoneMapper {

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "deliveryPersons", ignore = true)  
    @Mapping(target = "parcels", ignore = true)
    Zone toEntity(ZoneCreateDTO dto);

    @Mapping(target = "deliveryPersons", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    Zone toEntity(ZoneUpdateDTO dto);

    ZoneResponseDTO toResponseDTO(Zone entity);

    List<ZoneResponseDTO> toResponseDTOList(List<Zone> entities);

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "deliveryPersons", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    void updateEntityFromDTO(ZoneUpdateDTO dto, @MappingTarget Zone entity);
}
