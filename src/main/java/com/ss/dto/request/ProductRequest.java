package com.ss.dto.request;

import com.ss.model.FileModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Set;
import java.util.UUID;

@Data
public class ProductRequest {

    @NotBlank
    private String code;

    @NotBlank
    private String productNumber;

    @NotBlank
    private String name;

    private UUID categoryId;

    private UUID brandId;

    private Long costPrice;

    private Long soldPrice;

    private String color;

    private String size;

    private Set<UUID> imageIds;
}
