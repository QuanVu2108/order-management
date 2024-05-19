package com.ss.enums.excel;

import com.ss.util.excel.ExportTemplate;


public enum ProductExportExcel implements ExportTemplate {
    STT("KEY_STT", "STT"),
    IMAGE_URL("IMAGE_URL", "Image"),
    CODE("CODE", "Code"),
    PRODUCT_NUMBER("PRODUCT_NUMBER", "Product Number"),
    NAME("NAME", "Name"),
    CATEGORY("CATEGORY", "Category"),
    BRAND("BRAND", "Brand"),
    COLOR("COLOR", "Color"),
    SIZE("SIZE", "Size"),
    SOLD_PRICE("SOLD_PRICE", "Sold price"),
    COST_PRICE("COST_PRICE", "Cost price"),
    INCENTIVE("INCENTIVE", "Incentive"),
    UPDATED_TIME("UPDATED_TIME", "Updated Time"),
    ;
    private final String key;
    private final String title;

    ProductExportExcel(String key, String title) {
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