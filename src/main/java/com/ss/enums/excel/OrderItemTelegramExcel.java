package com.ss.enums.excel;

import com.ss.util.excel.ExportTemplate;


public enum OrderItemTelegramExcel implements ExportTemplate {
    STT("KEY_STT", "STT"),
    PRODUCT_NUMBER("PRODUCT_NUMBER", "Product Number"),
    IMAGE_URL("IMAGE_URL", "Image"),
    COLOR("COLOR", "Color"),
    SIZE("SIZE", "Size"),
    QUANTITY("QUANTITY", "Quantity")
    ;
    private final String key;
    private final String title;

    OrderItemTelegramExcel(String key, String title) {
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