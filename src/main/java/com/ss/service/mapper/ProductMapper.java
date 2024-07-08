package com.ss.service.mapper;

import com.ss.dto.response.ProductResponse;
import com.ss.model.ProductModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper extends ModelMapper<ProductModel, ProductResponse> {
}

