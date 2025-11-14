package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryCreateDTO;
import com.logismart.logismartv2.dto.deliveryhistory.DeliveryHistoryResponseDTO;
import com.logismart.logismartv2.entity.DeliveryHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DeliveryHistoryMapper {

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "changedAt", ignore = true)  
    @Mapping(target = "parcel", ignore = true)  
    DeliveryHistory toEntity(DeliveryHistoryCreateDTO dto);

    @Mapping(target = "parcelId", source = "parcel.id")
    @Mapping(target = "statusDisplay", expression = "java(entity.getStatusDisplay())")
    @Mapping(target = "formattedChangedAt", expression = "java(entity.getFormattedChangedAt())")
    @Mapping(target = "hasComment", expression = "java(entity.hasComment())")
    @Mapping(target = "summary", expression = "java(entity.getSummary())")
    @Mapping(target = "detailedSummary", expression = "java(entity.getDetailedSummary())")
    DeliveryHistoryResponseDTO toResponseDTO(DeliveryHistory entity);

    List<DeliveryHistoryResponseDTO> toResponseDTOList(List<DeliveryHistory> entities);
}
