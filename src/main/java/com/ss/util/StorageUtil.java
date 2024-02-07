//package com.ss.util;
//
//import com.google.auth.oauth2.GoogleCredentials;
//import com.google.cloud.storage.*;
//import com.ss.domain.FileDomain;
//import com.ss.exception.BadRequestError;
//import com.ss.exception.ExceptionResponse;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.commons.io.FileUtils;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.core.io.ClassPathResource;
//import org.springframework.stereotype.Component;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.FileOutputStream;
//import java.io.InputStream;
//import java.text.SimpleDateFormat;
//import java.util.Date;
//import java.util.UUID;
//
//@Slf4j
//@Component
//public class StorageUtil {
//
//    @Value("${gcp.config.server-domain}")
//    private String serverDomain;
//
//    @Value("${gcp.project.id}")
//    private String gcpProjectId;
//
//    @Value("${gcp.config.credential}")
//    private String gcpConfigCredential;
//
//    @Value("${gcp.bucket.id}")
//    private String gcpBucketId;
//
//    private File convertFile(MultipartFile file) {
//        try {
//            File convertedFile = new File(file.getOriginalFilename());
//            FileOutputStream outputStream = new FileOutputStream(convertedFile);
//            outputStream.write(file.getBytes());
//            outputStream.close();
//            log.debug("Converting file : {}", convertedFile);
//            return convertedFile;
//        } catch (Exception e) {
//            throw new ExceptionResponse("An error has occurred while converting the file " + file.getOriginalFilename(), BadRequestError.INVALID_INPUT);
//        }
//    }
//
//    public FileDomain uploadFile(MultipartFile multipartFile, FileType fileType, UUID rootId) {
//        String fileName = multipartFile.getOriginalFilename();
//        if (fileName == null) {
//            throw new ExceptionResponse("Original file name is null", BadRequestError.INVALID_INPUT);
//        }
//        try {
//            byte[] fileData = FileUtils.readFileToByteArray(convertFile(multipartFile));
//            InputStream inputStream = new ClassPathResource(gcpConfigCredential).getInputStream();
//            StorageOptions options = StorageOptions.newBuilder()
//                    .setProjectId(gcpProjectId)
//                    .setCredentials(GoogleCredentials.fromStream(inputStream))
//                    .build();
//            Storage storage = options.getService();
//            Bucket bucket = storage.get(gcpBucketId, Storage.BucketGetOption.fields());
//            Date date = new Date();
//            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
//            String stringDate= dateFormat.format(date);
//            UUID fileId = UUID.randomUUID();
//            Blob blob = bucket.create(fileType + "/" + stringDate + "/" + fileId + "_" + fileName, fileData);
//            if (blob == null) {
//                throw new ExceptionResponse("Can not upload file " + fileName, BadRequestError.INVALID_INPUT);
//            }
//            log.debug("File successfully uploaded to GCS");
//            return new FileDomain(blob.getName(), fileType, rootId, serverDomain  + "/" + gcpBucketId + "/" + blob.getName());
//        } catch (Exception e) {
//            log.error("An error occurred while uploading data. Exception: ", e);
//            throw new ExceptionResponse("An error occurred while storing data to GCS", BadRequestError.INVALID_INPUT);
//        }
//    }
//
//
//
//}
