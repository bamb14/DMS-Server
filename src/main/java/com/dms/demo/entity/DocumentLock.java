package com.dms.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "document_locks")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DocumentLock {

    @Id
    @Column(name = "doc_id")
    private Long docId; // 파일 ID 자체가 PK 역할

    @Column(name = "locked_by", nullable = false)
    private Long lockedBy; // 잠근 사람 ID

    @Column(name = "locked_at", nullable = false)
    private LocalDateTime lockedAt;

    @Column(name = "lock_expires_at")
    private LocalDateTime lockExpiresAt;

    @Column(name = "lock_reason", length = 500)
    private String lockReason;
}