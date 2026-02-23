package com.dms.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_id")
    private Long docId;

    @Column(name = "dir_id", nullable = false) // 문서는 폴더 없이 존재 불가
    private Long dirId;

    @Column(name = "display_name", nullable = false, length = 512)
    private String displayName;
    
    @Column(name = "original_name", nullable = false, length = 512)
    private String originalName;

    // [추가] 정규화된 이름 (검색 등을 위해 대소문자 통일이나 특수문자 처리된 이름)
    @Column(name = "normalized_name", nullable = false, length = 512)
    private String normalizedName;

    // [추가] 파일 확장자 (따로 저장해두면 아이콘 띄울 때 편함)
    @Column(name = "file_ext", length = 50, updatable = false)
    private String fileExt;

    @Column(name = "created_by", nullable = false, updatable = false)
    private Long createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // [추가] 수정 일시
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    @Column(name = "updated_by", nullable = false)
    private Long updatedBy;

    @Column(name = "latest_version_id")
    private Long latestVersionId;
    
    @Column(name = "latest_version_no", nullable = false)
    private Integer latestVersionNo;
    
    @Column(name = "latest_file_size", nullable=false)
    private Long latestFileSize;
    
    @Column(name = "updatedByName", nullable=false)
    private String updatedByName;
    
    public void updateLatestVersionInfo(DocumentVersion version, Long userId, String modifierName) {
        this.latestVersionId=version.getDocVersionId();
    	this.latestVersionNo = version.getVersionNo();
        this.latestFileSize = version.getSizeBytes();
        this.updatedAt = version.getCreatedAt();
        this.updatedBy =  userId;
        this.updatedByName = modifierName;
    }
    
    public void updateLatestVersionId(Long latestVersionId) {
    	this.latestVersionId = latestVersionId;
    }
    

    // [추가] 삭제 관련 (휴지통 기능)
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted=false;
    
    public void delete() {
        this.isDeleted = true;
    }
    
    public void rename(String newName) {
    	this.displayName=newName;
    	this.normalizedName=newName;
    }
    
//    @Column(name = "deleted_at")
//    private LocalDateTime deletedAt;
//
//    @Column(name = "purge_at")
//    private LocalDateTime purgeAt;
//
//    // [추가] 파일명 암호화 관련 (보안 요구사항)
//    @Column(name = "enc_display_name")
//    private byte[] encDisplayName;
//
//    @Column(name = "name_enc_alg", length = 50)
//    private String nameEncAlg;
//
//    @Column(name = "name_kid", length = 100)
//    private String nameKid;
}