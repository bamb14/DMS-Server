package com.dms.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_versions")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentVersion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "doc_version_id")
    private Long docVersionId;

    @Column(name = "doc_id", nullable = false)
    private Long docId;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;
    
    @Column(name = "version_file_name", nullable = false)
    private String versionFileName;

    @Column(name = "storage_key", nullable = false, length = 1024)
    private String storageKey;

    @Column(name = "size_bytes", nullable = false)
    private Long sizeBytes;

    @Column(name = "uploaded_by", nullable = false)
    private Long uploadedBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    public void rename(String newName) {
    	this.versionFileName=newName;
    }
}