package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.recipient.RecipientCreateDTO;
import com.logismart.logismartv2.dto.recipient.RecipientResponseDTO;
import com.logismart.logismartv2.dto.recipient.RecipientUpdateDTO;
import com.logismart.logismartv2.entity.Recipient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RecipientMapper {

    @Mapping(target = "id", ignore = true)  
    Recipient toEntity(RecipientCreateDTO dto);

    Recipient toEntity(RecipientUpdateDTO dto);

    @Mapping(target = "fullName", expression = "java(entity.getFullName())")
    @Mapping(target = "hasEmail", expression = "java(entity.hasEmail())")
    RecipientResponseDTO toResponseDTO(Recipient entity);

    List<RecipientResponseDTO> toResponseDTOList(List<Recipient> entities);

    @Mapping(target = "id", ignore = true)  
    void updateEntityFromDTO(RecipientUpdateDTO dto, @MappingTarget Recipient entity);
}
