package com.ss.enums.excel;

import com.ss.util.excel.ExportTemplate;


public enum OrderItemExportExcel implements ExportTemplate {
    STT("KEY_STT", "STT"),
    ORDER_CODE("CODE", "Code"),
    PRODUCT_CODE("PRODUCT_CODE", "Product Code"),
    PRODUCT_NUMBER("PRODUCT_NUMBER", "Product Number"),
    IMAGE_URL("IMAGE_URL", "Image"),
    STATUS("STATUS", "Status"),
    DATE("DATE", "Date"),
    QUANTITY("QUANTITY", "Quantity"),
    COST("COST", "Cost"),
    TOTAL_COST("TOTAL_COST", "Total Cost"),
    DELAY("DELAY", "Delay"),
    NOTE("NOTE", "Note"),
    CHECKED("CHECKED", "Checked"),
    IN_CART("IN_CART", "In cart"),
    RECEIVED("RECEIVED", "Received"),
    SENT("SENT", "Sent"),
    ;
    private final String key;
    private final String title;

    OrderItemExportExcel(String key, String title) {
        this.key = key;
        this.title = title;
    }

    public String getKey() {
        return key;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public int getStartCol() {
        return 0;
    }

    @Override
    public int getEndCol() {
        return 0;
    }

}