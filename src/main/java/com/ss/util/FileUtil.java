package com.ss.util;

import com.itextpdf.io.image.ImageData;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfDocumentInfo;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.InvalidInputError;
import com.ss.util.excel.ExportTemplate;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import static com.ss.enums.Const.TELEGRAM_IMAGE_HEIGHT;
import static com.ss.enums.Const.TELEGRAM_IMAGE_WITH;
import static com.ss.util.excel.ExcelUtil.*;

@Slf4j
@Component
public final class FileUtil {

    public static File createPdfWithTableAndImage(String title, List<String> data, List<List<String>> tableData, List<byte[]> imageBytes) {
        log.info("********* create pdf file");
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            PdfDocumentInfo info = pdf.getDocumentInfo();
            info.setTitle(title);
            Document document = new Document(pdf);

            // Create a table with the number of columns equal to the size of the first row
            float[] columnWidths = new float[tableData.get(0).size()];
            for (int i = 0; i < columnWidths.length; i++) {
                columnWidths[i] = 1; // Set all column widths to 1 for simplicity
            }

            data.forEach(item -> {
                Paragraph paragraph = new Paragraph(item);
                paragraph.setTextAlignment(TextAlignment.CENTER);
                document.add(paragraph);
            });

            Table table = new Table(columnWidths);

            // Add table header
            PdfFont font = PdfFontFactory.createFont("Helvetica-Bold");
            for (String header : tableData.get(0)) {
                table.addHeaderCell(new Cell().add(new Paragraph(header).setFont(font)));
            }
            table.setHorizontalAlignment(HorizontalAlignment.CENTER);

            // Add table rows
            font = PdfFontFactory.createFont("Helvetica");
            for (int i = 1; i < tableData.size(); i++) {
                for (int j = 0; j < tableData.get(i).size(); j++) {
                    String cellData = tableData.get(i).get(j);
                    if (cellData.equals("image_" + (i - 1)) && imageBytes.get(i - 1).length > 0) {
                        ImageData imageData = ImageDataFactory.create(imageBytes.get(i - 1));
                        Image image = new Image(imageData);
                        image.setWidth(TELEGRAM_IMAGE_WITH);
                        image.setHeight(TELEGRAM_IMAGE_HEIGHT);
                        table.addCell(new Cell().add(image));
                    } else {
                        table.addCell(new Cell().add(new Paragraph(cellData).setFont(font)));
                    }
                }
            }

            document.add(table);
            document.close();

            // Write the PDF to a temporary file
            java.io.File file = java.io.File.createTempFile(title, ".pdf");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(baos.toByteArray());
            fos.close();

            return file;
        } catch (Exception ex) {
            throw new ExceptionResponse(InvalidInputError.DOWNLOAD_FILE_FAILED.getMessage(), InvalidInputError.DOWNLOAD_FILE_FAILED);
        }
    }

    public static byte[] downloadImage(String imageUrl, String serverGcp) {
        if (!StringUtils.hasText(imageUrl))
            return new byte[0];
        try {
            imageUrl = imageUrl.replace(serverGcp, "http://localhost");
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Connect to the URL
            connection.connect();

            // Check if the response code indicates success
            int responseCode = connection.getResponseCode();
            // Read the response into a byte array
            try (InputStream inputStream = connection.getInputStream();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, bytesRead);
                }
                return outputStream.toByteArray();
            } catch (Exception ex) {
                log.error("************** image url was crush " + imageUrl);
                return new byte[0];
            }
        } catch (Exception ex) {
            log.error("************** image url was crush " + imageUrl);
            return new byte[0];
        }
    }

    public static File createExcelFile(List<Map<String, String>> assets, ExportTemplate[] columns, String fileName) {
        log.info("********* create xlsx file");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        Workbook workbook = null;
        try {
            workbook = getWorkbook(null, fileName + ".xlsx");
            CellStyle style = workbook.createCellStyle();
            style.setWrapText(true);
            Sheet sheet = workbook.createSheet("data");
            int rowHeaderIndex = 0;
            makeHeader(workbook, sheet, rowHeaderIndex, columns);

            makeContent(workbook, sheet, rowHeaderIndex, assets, columns);
            autoSizeColumns(sheet, columns.length);

            workbook.write(byteArrayOutputStream);

            java.io.File file = java.io.File.createTempFile(fileName, ".xlsx");
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(byteArrayOutputStream.toByteArray());
            fos.close();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            throw new ExceptionResponse(InvalidInputError.EXPORT_FILE_FAILED.getMessage(), InvalidInputError.EXPORT_FILE_FAILED);
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }

            try {
                byteArrayOutputStream.close();
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }
}
