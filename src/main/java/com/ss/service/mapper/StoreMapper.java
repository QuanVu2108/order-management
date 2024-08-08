package com.ss.service.mapper;

import com.ss.dto.response.StoreResponse;
import com.ss.model.StoreModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StoreMapper extends ModelMapper<StoreModel, StoreResponse> {
}
