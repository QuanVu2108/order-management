package com.ss.enums.excel;

import java.util.List;

public enum ProductCheckImportExcelTemplate {
    // coordinated progress
    STT("KEY_STT", 0, "STT"),
    CODE("CODE", 1, "Product Code"),
    NUMBER("NUMBER", 2, "Product Number"),
    STORE("STORE", 3, "Store"),
    QUANTITY("QUANTITY", 4, "Quantity"),
    COST("COST", 5, "Cost"),
    INCENTIVE("INCENTIVE", 6, "Incentive"),
    ;
    private final String key;
    private final int column;
    private final String title;

    ProductCheckImportExcelTemplate(String key, int column, String title) {
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


    public static List<ProductCheckImportExcelTemplate> getColumns() {
        return List.of(STT, CODE, NUMBER, STORE, QUANTITY, COST, INCENTIVE);
    }
}