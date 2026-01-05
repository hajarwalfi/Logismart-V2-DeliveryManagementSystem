package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.parcelproduct.ParcelProductCreateDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductResponseDTO;
import com.logismart.logismartv2.dto.parcelproduct.ParcelProductUpdateDTO;
import com.logismart.logismartv2.entity.ParcelProduct;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ParcelProductMapper {

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "addedAt", ignore = true)  
    @Mapping(target = "parcel", ignore = true)  
    @Mapping(target = "product", ignore = true)  
    ParcelProduct toEntity(ParcelProductCreateDTO dto);

    @Mapping(target = "addedAt", ignore = true)  
    @Mapping(target = "parcel", ignore = true)  
    @Mapping(target = "product", ignore = true)  
    ParcelProduct toEntity(ParcelProductUpdateDTO dto);

    @Mapping(target = "parcelId", source = "parcel.id")
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productName", expression = "java(entity.getProductName())")
    @Mapping(target = "formattedUnitPrice", expression = "java(entity.getFormattedUnitPrice())")
    @Mapping(target = "totalPrice", expression = "java(entity.getTotalPrice())")
    @Mapping(target = "formattedTotalPrice", expression = "java(entity.getFormattedTotalPrice())")
    @Mapping(target = "isBulkItem", expression = "java(entity.isBulkItem())")
    @Mapping(target = "lineItemSummary", expression = "java(entity.getLineItemSummary())")
    ParcelProductResponseDTO toResponseDTO(ParcelProduct entity);

    List<ParcelProductResponseDTO> toResponseDTOList(List<ParcelProduct> entities);

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "addedAt", ignore = true)  
    @Mapping(target = "parcel", ignore = true)  
    @Mapping(target = "product", ignore = true)  
    void updateEntityFromDTO(ParcelProductUpdateDTO dto, @MappingTarget ParcelProduct entity);
}
