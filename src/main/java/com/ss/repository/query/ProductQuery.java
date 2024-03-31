package com.ss.repository.query;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductQuery {
    private String code;
    private String number;
    private String name;
    private String brand;
    private String category;
    private String color;
    private String size;
}
