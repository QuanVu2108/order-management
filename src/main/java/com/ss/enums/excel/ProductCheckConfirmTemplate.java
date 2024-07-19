package com.ss.enums.excel;

public enum ProductCheckConfirmTemplate {
    NUMBER("NUMBER", 1, "Product Number"),
    ORDER_CODE("ORDER_CODE", 2, "Order Code"),
    STORE("STORE", 3, "Store"),
    QUANTITY("QUANTITY", 4, "Quantity"),
    ;
    private final String key;
    private final int column;
    private final String title;

    ProductCheckConfirmTemplate(String key, int column, String title) {
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