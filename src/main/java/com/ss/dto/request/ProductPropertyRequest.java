package com.ss.dto.request;

import com.ss.enums.ProductPropertyType;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class ProductPropertyRequest {

    @NotBlank
    private String name;

    @NotNull
    private ProductPropertyType type;

}
