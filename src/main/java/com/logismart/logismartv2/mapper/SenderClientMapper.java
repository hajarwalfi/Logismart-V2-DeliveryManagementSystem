package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.senderclient.SenderClientCreateDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientResponseDTO;
import com.logismart.logismartv2.dto.senderclient.SenderClientUpdateDTO;
import com.logismart.logismartv2.entity.SenderClient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SenderClientMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    SenderClient toEntity(SenderClientCreateDTO dto);

    @Mapping(target = "parcels", ignore = true)
    SenderClient toEntity(SenderClientUpdateDTO dto);

    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    SenderClientResponseDTO toResponseDTO(SenderClient entity);

    List<SenderClientResponseDTO> toResponseDTOList(List<SenderClient> entities);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parcels", ignore = true)
    void updateEntityFromDTO(SenderClientUpdateDTO dto, @MappingTarget SenderClient entity);
}
