package com.ss.dto.request;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.UUID;

@Data
public class ProductRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String name;

    private UUID categoryId;

    private UUID brandId;

    private Long costPrice;

    private Long soldPrice;

    private String color;

    private String size;
}
