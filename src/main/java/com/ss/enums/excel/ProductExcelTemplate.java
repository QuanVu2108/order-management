package com.ss.enums.excel;

import java.util.List;

public enum ProductExcelTemplate {
    // coordinated progress
//    STT("KEY_STT", 0, "STT"),
    PRODUCT_NUMBER("PRODUCT_NUMBER", 0, "Product Number"),
    IMAGE_URL("IMAGE_URL", 1, "Image"),
    CODE("CODE", 2, "Code"),
    NAME("NAME", 3, "Name"),
    CATEGORY("CATEGORY", 4, "Category"),
    BRAND("BRAND", 5, "Brand"),
    COLOR("COLOR", 6, "Color"),
    SIZE("SIZE", 7, "Size"),
    SOLD_PRICE("SOLD_PRICE", 8, "Sold price"),
    COST_PRICE("COST_PRICE", 9, "Cost price"),
    INCENTIVE("INCENTIVE", 10, "Incentive"),
    ;
    private final String key;
    private final int column;
    private final String title;

    ProductExcelTemplate(String key, int column, String title) {
        this.key = key;
        this.column = column;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    public int getColumn() {
        return column;
    }


    public static List<ProductExcelTemplate> getColumns() {
        return List.of(PRODUCT_NUMBER, IMAGE_URL, CODE, NAME, CATEGORY, BRAND, COLOR, SIZE, SOLD_PRICE, COST_PRICE, INCENTIVE);
    }
}