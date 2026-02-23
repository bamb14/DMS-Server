package com.dms.demo.dto;

import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileDownloadDto {
    private Resource resource;      // 실제 파일 데이터
    private String originalFileName; // 사용자가 다운로드 받을 때 볼 파일명
    private String contentType;     // 파일 타입 (예: application/pdf)
}