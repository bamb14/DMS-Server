package com.dms.demo.dto;

import java.time.LocalDateTime;

import com.dms.demo.entity.Document;
import com.dms.demo.entity.DocumentVersion;
import com.dms.demo.util.ByteConverter;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class DocumentDto{
	
	@Getter
    public static class DocumentResponseDto {
        private Long docId;
        private String name;
        private String type;
        private String size;
        private Long latestVersionId;
        private Long uploaderId;
        private String uploader;
        private LocalDateTime updatedAt;
        private Long dirId;
        @Builder.Default
        private Boolean isLocked=false;
        private String lockReason;
        private Long lockedBy;

        public DocumentResponseDto(Document document, Long updatedBy, String updatedByName, boolean isLocked, String lockReason, Long lockedBy) {
            this.docId = document.getDocId();
            this.name = document.getDisplayName();
            this.type=document.getFileExt();
            this.size=ByteConverter.format(document.getLatestFileSize());
            this.uploaderId = updatedBy;
            this.uploader = updatedByName;
            this.updatedAt = document.getUpdatedAt();
            this.dirId=document.getDirId();
            this.latestVersionId = document.getLatestVersionId();
            this.isLocked = isLocked;
            this.lockReason = lockReason;
            this.lockedBy = lockedBy;
        }
    }
	
	// 이름 변경 요청용
    @Data
    public static class RenameRequest {
        private String newName;
    }
    
    @Getter
     public static class DocumentVersionDto {
    	private Long versionId;
    	private Long docId;
    	private Integer version;
    	private String versionFileName;
    	private String size;
    	private String type;
    	private String uploader;
    	private LocalDateTime createdAt;
    	
    	public DocumentVersionDto(DocumentVersion version, String type, String uploader) {
    		this.versionId=version.getDocVersionId();
    		this.docId=version.getDocId();
    		this.version=version.getVersionNo();
    		this.versionFileName=version.getVersionFileName();
    		this.size=ByteConverter.format(version.getSizeBytes());
    		this.type=type;
    		this.uploader = uploader;
    		this.createdAt=version.getCreatedAt();
    	}
    }
}