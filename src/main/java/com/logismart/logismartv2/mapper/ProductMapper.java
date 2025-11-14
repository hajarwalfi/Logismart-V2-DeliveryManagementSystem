package com.logismart.logismartv2.mapper;

import com.logismart.logismartv2.dto.product.ProductCreateDTO;
import com.logismart.logismartv2.dto.product.ProductResponseDTO;
import com.logismart.logismartv2.dto.product.ProductUpdateDTO;
import com.logismart.logismartv2.entity.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "parcelProducts", ignore = true)  
    Product toEntity(ProductCreateDTO dto);

    @Mapping(target = "parcelProducts", ignore = true)
    Product toEntity(ProductUpdateDTO dto);

    @Mapping(target = "hasCategory", expression = "java(entity.hasCategory())")
    @Mapping(target = "formattedWeight", expression = "java(entity.getFormattedWeight())")
    @Mapping(target = "formattedPrice", expression = "java(entity.getFormattedPrice())")
    ProductResponseDTO toResponseDTO(Product entity);

    List<ProductResponseDTO> toResponseDTOList(List<Product> entities);

    @Mapping(target = "id", ignore = true)  
    @Mapping(target = "parcelProducts", ignore = true)
    void updateEntityFromDTO(ProductUpdateDTO dto, @MappingTarget Product entity);
}
