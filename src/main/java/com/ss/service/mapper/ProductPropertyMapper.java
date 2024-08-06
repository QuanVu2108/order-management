package com.ss.service.mapper;

import com.ss.dto.response.ProductPropertyResponse;
import com.ss.model.ProductPropertyModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductPropertyMapper extends ModelMapper<ProductPropertyModel, ProductPropertyResponse> {
}
