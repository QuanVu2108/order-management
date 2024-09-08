package com.ss.util;

import com.google.cloud.NoCredentials;
import com.google.cloud.storage.*;
import com.ss.exception.BadRequestError;
import com.ss.exception.ExceptionResponse;
import com.ss.model.FileModel;
import io.opencensus.resource.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import static com.ss.enums.Const.IMAGE_CONTENT_TYPE;

@Slf4j
@Component
public class StorageUtil {

    @Value("${gcp.server}")
    private String serverGcp;

    @Value("${gcp.domain}")
    private String domainGcp;

    @Value("${gcp.port}")
    private String portGcp;

    @Value("${gcp.project.id}")
    private String gcpProjectId;

    @Value("${gcp.bucket.id}")
    private String gcpBucketId;

    public FileModel uploadFile(MultipartFile file) {
        if (file == null)
            return null;
        String fileName = file.getOriginalFilename();
        if (fileName == null) {
            throw new ExceptionResponse("Original file name is null");
        }
        Path path = new File(fileName).toPath();
        try {
            Storage storage = getStorage();
            String contentType = Files.probeContentType(path);
            log.debug("Start file uploading process on GCS");
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(file));

            UUID bucketId = UUID.randomUUID();

            Bucket bucket = storage.create(BucketInfo.newBuilder(bucketId.toString()).build());

            Bucket.BlobTargetOption targetOption = Bucket.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ_WRITE);
            Blob blob = bucket.create(fileName, fileData, contentType, targetOption);

            if (blob == null) {
                throw new ExceptionResponse("Can not upload file " + fileName);
            }
            log.debug("File successfully uploaded to GCS");
            return FileModel.builder()
                    .id(UUID.randomUUID())
                    .name(blob.getName())
                    .url(generateFileUrl(bucketId, fileName))
                    .build();

        } catch (Exception e) {
            log.error("An error occurred while uploading data. Exception: ", e);
            throw new ExceptionResponse("An error occurred while storing data to GCS");
        }
    }

    public String uploadFileByUrl(String fileName, String url) {
        byte[] fileBytes = FileUtil.downloadImage(url, domainGcp);
        MultipartFile file = new CustomMultipartFile(fileBytes, fileName, IMAGE_CONTENT_TYPE);
        if (file == null)
            return null;
        if (fileName == null) {
            throw new ExceptionResponse("Original file name is null");
        }
        Path path = new File(fileName).toPath();
        try {
            Storage storage = getStorage();
            String contentType = Files.probeContentType(path);
            log.info("Start file uploading process on GCS");
            byte[] fileData = FileUtils.readFileToByteArray(convertFile(file));

            UUID bucketId = UUID.randomUUID();

            Bucket bucket = storage.create(BucketInfo.newBuilder(bucketId.toString()).build());

            Bucket.BlobTargetOption targetOption = Bucket.BlobTargetOption.predefinedAcl(Storage.PredefinedAcl.PUBLIC_READ_WRITE);
            Blob blob = bucket.create(fileName, fileData, contentType, targetOption);

            if (blob == null) {
                throw new ExceptionResponse("Can not upload file " + fileName);
            }
            log.info("File successfully uploaded to GCS");
            return generateFileUrl(bucketId, fileName);

        } catch (Exception e) {
            log.error("An error occurred while uploading data. Exception: ", e);
            throw new ExceptionResponse("An error occurred while storing data to GCS");
        }
    }

    private String generateFileUrl(UUID bucketId, String fileName) {
        StringBuilder url = new StringBuilder(domainGcp + portGcp);
        url.append("/storage/v1/b/");
        url.append(bucketId);
        url.append("/o/");
        url.append(fileName);
        url.append("?alt=media");
        return url.toString();
    }

    public Storage getStorage() {
        return StorageOptions.newBuilder()
                .setHost(serverGcp + portGcp)
                .setProjectId(gcpProjectId)
                .setCredentials(NoCredentials.getInstance())
                .build()
                .getService();
    }

    private File convertFile(MultipartFile file) {
        try {
            File convertedFile = new File(file.getOriginalFilename());
            FileOutputStream outputStream = new FileOutputStream(convertedFile);
            outputStream.write(file.getBytes());
            outputStream.close();
            log.debug("Converting file : {}", convertedFile);
            return convertedFile;
        } catch (Exception e) {
            throw new ExceptionResponse("An error has occurred while converting the file " + file.getOriginalFilename(), BadRequestError.INVALID_INPUT);
        }
    }

}
