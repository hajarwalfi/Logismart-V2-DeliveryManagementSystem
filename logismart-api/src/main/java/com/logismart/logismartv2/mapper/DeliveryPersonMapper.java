package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonCreateDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonResponseDTO;
import com.logismart.logismartv2.dto.deliveryperson.DeliveryPersonUpdateDTO;
import com.logismart.logismartv2.entity.DeliveryPerson;
import com.logismart.logismartv2.entity.Zone;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryPersonMapper {

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "assignedZone", ignore = true)  
    @Mapping(target = "parcels", ignore = true)  
    DeliveryPerson toEntity(DeliveryPersonCreateDTO dto);

    @Mapping(target = "assignedZone", ignore = true)  
    @Mapping(target = "parcels", ignore = true)
    DeliveryPerson toEntity(DeliveryPersonUpdateDTO dto);

    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    @Mapping(target = "hasVehicle", expression = "java(entity.hasVehicle())")
    @Mapping(target = "assignedZoneId", source = "assignedZone.id")
    @Mapping(target = "assignedZoneName", source = "assignedZone.name")
    @Mapping(target = "hasAssignedZone", expression = "java(entity.hasAssignedZone())")
    DeliveryPersonResponseDTO toResponseDTO(DeliveryPerson entity);

    List<DeliveryPersonResponseDTO> toResponseDTOList(List<DeliveryPerson> entities);

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "assignedZone", ignore = true)  
    @Mapping(target = "parcels", ignore = true)
    void updateEntityFromDTO(DeliveryPersonUpdateDTO dto, @MappingTarget DeliveryPerson entity);

    @Named("zoneIdToZone")
    default Zone mapZoneId(String zoneId) {
        if (zoneId == null) {
            return null;
        }
        Zone zone = new Zone();
        zone.setId(zoneId);
        return zone;
    }
}
