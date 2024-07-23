package com.ss.enums.excel;

public enum ProductCheckImportKiotvietExcelTemplate {
    NUMBER("NUMBER", 1, "Product Number"),
    QUANTITY("QUANTITY", 18, "Quantity")
    ;
    private final String key;
    private final int column;
    private final String title;

    ProductCheckImportKiotvietExcelTemplate(String key, int column, String title) {
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

}