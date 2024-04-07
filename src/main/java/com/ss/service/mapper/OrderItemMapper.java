package com.ss.service.mapper;

import com.ss.dto.response.OrderItemResponse;
import com.ss.model.OrderItemModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemMapper extends ModelMapper<OrderItemModel, OrderItemResponse> {
}

