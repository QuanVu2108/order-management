package com.ss.util.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Slf4j
public class ExcelUtil {

    public static Workbook getWorkbook(InputStream inputStream, String excelFilePath) throws IOException {
        Workbook workbook = null;
        if (excelFilePath.endsWith("xlsx")) {
            workbook = inputStream == null ? new XSSFWorkbook() : new XSSFWorkbook(inputStream);
        } else if (excelFilePath.endsWith("xls")) {
            workbook = inputStream == null ? new HSSFWorkbook() : new HSSFWorkbook(inputStream);
        } else {
            throw new IllegalArgumentException("The specified file is not Excel file");
        }
        return workbook;
    }

    public static boolean isRowEmpty(Row row) {
        boolean isEmpty = true;
        DataFormatter dataFormatter = new DataFormatter();
        if (row != null) {
            for (Cell cell : row) {
                if (dataFormatter.formatCellValue(cell).trim().length() > 0) {
                    isEmpty = false;
                    break;
                }
            }
        }
        return isEmpty;
    }

    public static List<Map<String, String>> readUploadFileData(InputStream inputStream, String filename, List<ExcelTemplate> columnKeys, int startRow, int sheetIdx, List<String> typeDateColumns) throws Exception {
        List<Map<String, String>> data = new ArrayList<>();

        Workbook workbook = getWorkbook(inputStream, filename);
        Sheet sheet = workbook.getSheetAt(sheetIdx);
        int numOfRows = sheet.getPhysicalNumberOfRows();

        for (int i = startRow; i < numOfRows; i++) {
            Row row = sheet.getRow(i);
            if (isRowEmpty(row)) {
                break;
            }
            Map<String, String> rawData = new HashMap<>();
            for (ExcelTemplate column : columnKeys) {
                var rowCell = row.getCell(column.getColumn());
                if (rowCell != null) {
                    if (typeDateColumns.contains(column.getKey())) {
                        try {
                            Date dateColumn = rowCell.getDateCellValue();
                            Long date = dateColumn.getTime();
                            rawData.put(column.getKey(), String.valueOf(date));
                        } catch (Exception ex) {
                            log.error("date value is invalid " + column.getKey());
                        }
                    } else if (rowCell.getCellType().equals(CellType.STRING)) {
                        rawData.put(column.getKey(), rowCell.getStringCellValue().trim());
                    } else if (rowCell.getCellType().equals(CellType.NUMERIC)) {
                        rawData.put(column.getKey(), convertDoubleString(String.valueOf(rowCell.getNumericCellValue())));
                    }
                }
            }

            if (rawData.size() > 0) {
                data.add(rawData);
            }
        }

        return data;
    }

    private static String convertDoubleString(String val) {
        if (val.endsWith(".0")) {
            return val.substring(0, val.lastIndexOf("."));
        }
        return val;
    }

    public static Sheet createSheetUnit(Workbook workbook, String unitRangeDVConstraint, List<String> columnNames) {
        if (columnNames == null || columnNames.size() == 0)
            throw new IllegalArgumentException("columns of Excel file is invalid");
        String sheetNameUnit = "Master Data";
        Sheet unitSheet = workbook.createSheet(sheetNameUnit);
        Row row;
        Name namedRange;
        String colLetter;
        String reference;
        int column = 0;
        //Put data to sheet
        int r = 0;
        for (String unit : columnNames) {
            row = unitSheet.getRow(r);
            if (row == null) {
                row = unitSheet.createRow(r);
                row.createCell(column).setCellValue(unit);
            }
            r++;
        }
        //create names for the item list constraints, each named from the current key
        colLetter = CellReference.convertNumToColString(column);
        namedRange = workbook.createName();
        namedRange.setNameName(unitRangeDVConstraint);
        reference = sheetNameUnit + "!$" + colLetter + "$2:$" + colLetter + "$" + r;
        namedRange.setRefersToFormula(reference);

        //unselect that sheet because we will hide it later
        unitSheet.setSelected(false);
        return unitSheet;
    }

    public static Sheet createSheetUnit(Workbook workbook, String unitRangeDVConstraint, List<String> columnNames, List<ExcelTemplate> columnTitleParents) {
        if (columnNames == null || columnNames.size() == 0)
            throw new IllegalArgumentException("columns of Excel file is invalid");
        String sheetNameUnit = "Data";
        Sheet unitSheet = workbook.createSheet(sheetNameUnit);
        Row row;
        Name namedRange;
        String colLetter;
        String reference;
        // put parent
        for (ExcelTemplate unit : columnTitleParents) {
            unitSheet.addMergedRegion(new CellRangeAddress(0, 0, unit.getStartCol(), unit.getEndCol()));
        }

        //Put data to sheet
        int column = 0;
        int r = 1;
        for (String unit : columnNames) {
            row = unitSheet.getRow(r);
            if (row == null) {
                row = unitSheet.createRow(r);
                row.createCell(column).setCellValue(unit);
            }
            r++;
        }
        //create names for the item list constraints, each named from the current key
        colLetter = CellReference.convertNumToColString(column);
        namedRange = workbook.createName();
        namedRange.setNameName(unitRangeDVConstraint);
        reference = sheetNameUnit + "!$" + colLetter + "$2:$" + colLetter + "$" + r;
        namedRange.setRefersToFormula(reference);

        //unselect that sheet because we will hide it later
        unitSheet.setSelected(false);
        return unitSheet;
    }

    public static Row createCellHeader(Sheet sheet, List<String> columnNames) {
        Row rowHeader = sheet.createRow(1);
        for (int i = 0; i < columnNames.size(); i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(columnNames.get(i));
        }
        return rowHeader;
    }

    public static List<Row> createCellHeader(Sheet sheet, List<ExcelTemplate> columnTitleParents, List<String> columnNames) {
        List<Row> rowHeaders = new ArrayList<>();
        Row rowHeaderParent = sheet.createRow(0);
        for (int i = 0; i < columnTitleParents.size(); i++) {
            ExcelTemplate columnTitleParent = columnTitleParents.get(i);
            Cell cell = rowHeaderParent.createCell(columnTitleParent.getStartCol());
            cell.setCellValue(columnTitleParent.getTitle());
            sheet.addMergedRegion(new CellRangeAddress(0, 0, columnTitleParent.getStartCol(), columnTitleParent.getEndCol()));
        }
        rowHeaders.add(rowHeaderParent);

        Row rowHeader = sheet.createRow(1);
        for (int i = 0; i < columnNames.size(); i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellValue(columnNames.get(i));
        }
        rowHeaders.add(rowHeader);
        return rowHeaders;
    }

    public static void autoSizeColumns(Sheet sheet, int size) {
        for (int i = 0; i < size; i++) {
            sheet.autoSizeColumn(i);
        }
    }


    public static void makeContent(Workbook workbook, Sheet sheet, int rowHeaderIndex, List<Map<String, String>> content, ExcelTemplate[] columns) {
        var contentStyle = workbook.createCellStyle();
        contentStyle.setBorderTop(BorderStyle.THIN);
        contentStyle.setBorderBottom(BorderStyle.THIN);
        contentStyle.setBorderLeft(BorderStyle.THIN);
        contentStyle.setBorderRight(BorderStyle.THIN);

        int contentRowIndex = rowHeaderIndex + 1;
        if (content == null) {
            while (contentRowIndex < 10) {
                Row contentRow = sheet.createRow(contentRowIndex);
                for (int j = 0; j < columns.length - 1; j++) {
                    Cell cell = contentRow.createCell(j);
                    cell.setCellStyle(contentStyle);
                    cell.setCellValue("");
                }
                contentRowIndex++;
            }
        } else {
            for (Map<String, String> item : content) {
                Row contentRow = sheet.createRow(contentRowIndex);
                for (int j = 0; j < columns.length; j++) {
                    Cell cell = contentRow.createCell(j);
                    cell.setCellStyle(contentStyle);
                    cell.setCellValue(item.getOrDefault(columns[j].getKey(), ""));
                }
                contentRowIndex++;
            }
        }
    }

    public static void makeHeader(Workbook workbook, Sheet sheet, int rowHeaderIndex, ExcelTemplate[] columns) {
        Row rowHeader = sheet.createRow(rowHeaderIndex);
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setWrapText(true);
        Font font = workbook.createFont();
        font.setBold(true);
        headerStyle.setFont(font);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        // set header
        for (int i = 0; i < columns.length; i++) {
            Cell cell = rowHeader.createCell(i);
            cell.setCellStyle(headerStyle);
            cell.setCellValue(columns[i].getTitle());
        }
    }

    public static CellStyle getHeaderStyle(Workbook workbook) {
        CellStyle headerStyle = workbook.createCellStyle();
        headerStyle.setWrapText(true);
        headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);
        return headerStyle;
    }

}
