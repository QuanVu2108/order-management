package com.ss.util;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.ss.exception.ExceptionResponse;
import com.ss.exception.http.InvalidInputError;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ss.enums.Const.QR_CODE_HEIGHT;
import static com.ss.enums.Const.QR_CODE_WITH;

public final class QRCodeUtil {
    public static byte[] generateQRCodeImage(String text) {
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, QR_CODE_WITH, QR_CODE_HEIGHT);

            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        } catch (Exception ex) {
            throw new ExceptionResponse(InvalidInputError.GENERATE_QR_FAILED.getMessage(), InvalidInputError.GENERATE_QR_FAILED);
        }
        return pngOutputStream.toByteArray();
    }

}
