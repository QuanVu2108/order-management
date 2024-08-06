package com.ss.dto.response;


import com.ss.enums.ProductPropertyType;
import lombok.Data;

import java.util.UUID;

@Data
public class ProductPropertyResponse {
    private UUID id;

    private String code;

    private String name;

    private String description;

    private ProductPropertyType type;

}
