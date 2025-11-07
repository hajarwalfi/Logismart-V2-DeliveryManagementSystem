package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.parcel.ParcelCreateDTO;
import com.logismart.logismartv2.dto.parcel.ParcelResponseDTO;
import com.logismart.logismartv2.dto.parcel.ParcelUpdateDTO;
import com.logismart.logismartv2.entity.Parcel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParcelMapper {

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "createdAt", ignore = true)  
    @Mapping(target = "senderClient", ignore = true)  
    @Mapping(target = "recipient", ignore = true)  
    @Mapping(target = "deliveryPerson", ignore = true)  
    @Mapping(target = "zone", ignore = true)  
    @Mapping(target = "parcelProducts", ignore = true)  
    @Mapping(target = "deliveryHistories", ignore = true)  
    @Mapping(target = "status", constant = "CREATED")  
    Parcel toEntity(ParcelCreateDTO dto);

    @Mapping(target = "description", ignore = true)  
    @Mapping(target = "weight", ignore = true)  
    @Mapping(target = "priority", ignore = true)  
    @Mapping(target = "destinationCity", ignore = true)  
    @Mapping(target = "createdAt", ignore = true)  
    @Mapping(target = "senderClient", ignore = true)  
    @Mapping(target = "recipient", ignore = true)  
    @Mapping(target = "deliveryPerson", ignore = true)  
    @Mapping(target = "zone", ignore = true)  
    @Mapping(target = "parcelProducts", ignore = true)  
    @Mapping(target = "deliveryHistories", ignore = true)  
    Parcel toEntity(ParcelUpdateDTO dto);

    @Mapping(target = "formattedWeight", expression = "java(entity.getFormattedWeight())")
    @Mapping(target = "statusDisplay", expression = "java(entity.getStatusDisplay())")
    @Mapping(target = "priorityDisplay", expression = "java(entity.getPriorityDisplay())")
    @Mapping(target = "senderClientId", source = "senderClient.id")
    @Mapping(target = "senderClientName", expression = "java(entity.getSenderName())")
    @Mapping(target = "recipientId", source = "recipient.id")
    @Mapping(target = "recipientName", expression = "java(entity.getRecipientName())")
    @Mapping(target = "deliveryPersonId", source = "deliveryPerson.id")
    @Mapping(target = "deliveryPersonName", expression = "java(entity.getDeliveryPersonName())")
    @Mapping(target = "zoneId", source = "zone.id")
    @Mapping(target = "zoneName", expression = "java(entity.getZoneName())")
    @Mapping(target = "totalValue", expression = "java(entity.getTotalValue())")
    @Mapping(target = "formattedTotalValue", expression = "java(entity.getFormattedTotalValue())")
    @Mapping(target = "productCount", expression = "java(entity.getProductCount())")
    @Mapping(target = "isDelivered", expression = "java(entity.isDelivered())")
    @Mapping(target = "isInProgress", expression = "java(entity.isInProgress())")
    @Mapping(target = "isHighPriority", expression = "java(entity.isHighPriority())")
    @Mapping(target = "isAssignedToDeliveryPerson", expression = "java(entity.isAssignedToDeliveryPerson())")
    ParcelResponseDTO toResponseDTO(Parcel entity);

    List<ParcelResponseDTO> toResponseDTOList(List<Parcel> entities);

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "description", ignore = true)
    @Mapping(target = "weight", ignore = true)
    @Mapping(target = "priority", ignore = true)
    @Mapping(target = "destinationCity", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "senderClient", ignore = true)
    @Mapping(target = "recipient", ignore = true)
    @Mapping(target = "deliveryPerson", ignore = true)
    @Mapping(target = "zone", ignore = true)
    @Mapping(target = "parcelProducts", ignore = true)
    @Mapping(target = "deliveryHistories", ignore = true)
    void updateEntityFromDTO(ParcelUpdateDTO dto, @MappingTarget Parcel entity);
}
