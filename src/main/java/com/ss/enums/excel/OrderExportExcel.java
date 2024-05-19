package com.ss.enums.excel;

import com.ss.util.excel.ExportTemplate;


public enum OrderExportExcel implements ExportTemplate {
    STT("KEY_STT", "STT"),
    CODE("CODE", "Code"),
    DATE("DATE", "Date"),
    STATUS("STATUS", "Status"),
    PRODUCT_CODE("PRODUCT_CODE", "Product Code"),
    PRODUCT_NUMBER("PRODUCT_NUMBER", "Product Number"),
    TOTAL_QUANTITY("TOTAL_QUANTITY", "Total Quantity"),
    TOTAL_COST("TOTAL_COST", "Total Cost"),
    INCENTIVE("INCENTIVE", "Incentive"),
    UPDATED_TIME("UPDATED_TIME", "Updated Time"),
    NOTE("NOTE", "Note"),
    ;
    private final String key;
    private final String title;

    OrderExportExcel(String key, String title) {
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