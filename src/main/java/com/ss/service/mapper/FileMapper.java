package com.ss.service.mapper;

import com.ss.dto.response.FileResponse;
import com.ss.model.FileModel;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface FileMapper extends ModelMapper<FileModel, FileResponse> {
}

