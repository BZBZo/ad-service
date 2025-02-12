package com.example.spring.bzadservice.config.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@Slf4j
public class S3Uploader {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public S3Uploader(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * MultipartFile을 S3에 업로드
     */
    public String uploadFileToS3(MultipartFile multipartFile, String filePath) {
        if (multipartFile.isEmpty()) {
            throw new IllegalArgumentException("File is empty.");
        }

        File uploadFile = convert(multipartFile)
                .orElseThrow(() -> new IllegalArgumentException("[Error]: MultipartFile -> File 변환 실패"));

        String fileName = filePath + UUID.randomUUID();
        String uploadImageUrl = putS3(uploadFile, fileName);
        removeNewFile(uploadFile);

        return uploadImageUrl;
    }

    /**
     * S3에 파일 업로드
     */
    public String putS3(File uploadFile, String fileName) {
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucket)
                .key(fileName)
                .acl("public-read")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromFile(uploadFile));
        log.info("[S3Uploader] : S3에 파일 업로드 성공 - " + fileName);

        return String.format("https://%s.s3.amazonaws.com/%s", bucket, fileName);
    }

    /**
     * S3에서 파일 삭제
     */
    public void deleteS3(String filePath) {
        String key = filePath.substring(filePath.indexOf("/") + 1); // 파일 경로에서 키 추출

        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
        log.info("[S3Uploader] : S3에서 파일 삭제 성공 - " + filePath);
    }

    /**
     * 로컬에 저장된 파일 삭제
     */
    private void removeNewFile(File targetFile) {
        if (targetFile.delete()) {
            log.info("[S3Uploader] : 로컬 파일 삭제 성공");
        } else {
            log.warn("[S3Uploader] : 로컬 파일 삭제 실패");
        }
    }

    /**
     * MultipartFile -> File 변환
     */
    private Optional<File> convert(MultipartFile file) {
        String dirPath = System.getProperty("user.dir") + "/" + file.getOriginalFilename();
        File convertFile = new File(dirPath);

        try (FileOutputStream fos = new FileOutputStream(convertFile)) {
            fos.write(file.getBytes());
            return Optional.of(convertFile);
        } catch (IOException e) {
            log.error("[S3Uploader] : 파일 변환 중 오류 발생", e);
            return Optional.empty();
        }
    }
}