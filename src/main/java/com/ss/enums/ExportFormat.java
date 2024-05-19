package com.ss.enums;

import lombok.Getter;

@Getter
public enum ExportFormat {
    EXCEL("excel", ".xlsx"),
    CSV("csv", ".csv");
    private String extention;
    private String format;

    ExportFormat(String format, String extention){
        this.extention = extention;
        this.format = format;
    }

    public String getExtension(){
        return extention;
    }

    public String getFormat(){
        return format;
    }
}
