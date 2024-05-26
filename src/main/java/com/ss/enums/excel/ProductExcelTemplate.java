package com.ss.enums.excel;

import java.util.List;

public enum ProductExcelTemplate {
    STT("KEY_STT", 0, "STT"),
    PRODUCT_NUMBER("PRODUCT_NUMBER", 1, "Product Number"),
    IMAGE_URL("IMAGE_URL", 2, "Image"),
    CODE("CODE", 3, "Code"),
    NAME("NAME", 4, "Name"),
    CATEGORY("CATEGORY", 5, "Category"),
    BRAND("BRAND", 6, "Brand"),
    COLOR("COLOR", 7, "Color"),
    SIZE("SIZE", 8, "Size"),
    SOLD_PRICE("SOLD_PRICE", 9, "Sold price"),
    COST_PRICE("COST_PRICE", 10, "Cost price"),
    INCENTIVE("INCENTIVE", 11, "Incentive"),
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