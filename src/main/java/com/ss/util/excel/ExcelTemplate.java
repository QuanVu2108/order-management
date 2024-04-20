package com.ss.util.excel;

import lombok.Data;

@Data
public class ExcelTemplate {
    private String key;

    private int column;
    private String title;

    private int startCol;

    private int endCol;

    public ExcelTemplate(String key, int column) {
        this.key = key;
        this.column = column;
    }

    public ExcelTemplate(String title, int startCol, int endCol) {
        this.title = title;
        this.startCol = startCol;
        this.endCol = endCol;
    }
}
