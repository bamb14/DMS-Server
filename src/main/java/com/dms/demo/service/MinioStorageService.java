package com.dms.demo.service;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MinioStorageService {
	private final MinioClient minioClient;
	
	@Value("${minio.bucket-name}")
    private String bucketName;
	
	public void uploadToMinio(MultipartFile file, String storageKey) {
        try {
            InputStream inputStream = file.getInputStream();
            String contentType = file.getContentType();
            
            // Content-Type이 없으면 기본값 설정 (다운로드 시 문제 방지)
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            // MinIO에 업로드 (PutObjectArgs 사용)
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .stream(inputStream, file.getSize(), -1) // -1은 part size 자동 설정
                            .contentType(contentType)
                            // 서버 측 암호화 추가
//                            .sse(new ServerSideEncryptionS3())
                            .build()
            );
            
            inputStream.close();
            
        } catch (Exception e) {
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다.", e);
        }
    }

    public String getDownloadUrl(String storageKey, String fileName) {
        try {
        	String encodedFileName = UriUtils.encode(fileName, StandardCharsets.UTF_8);
            
        	Map<String, String> queryParams = new HashMap<>();
            queryParams.put("response-content-disposition", 
                    "attachment; filename=\"" + encodedFileName + "\"");
        	
        	return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .bucket(bucketName)
                            .object(storageKey)
                            .method(Method.GET)
                            .expiry(1, TimeUnit.HOURS)
                            .extraQueryParams(queryParams)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("다운로드 URL을 생성하는 도중 오류가 발생했습니다.", e);
        }
    }
}