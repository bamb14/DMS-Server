package com.dms.demo.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "directory_permissions")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DirectoryPermission {

	@Id
	@Column(name = "perm_id")
    private Long permId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dir_id", nullable = false)
    private Directory directory;

    @Column(name = "dept_id", nullable = false)
    private Long deptId;

    // 생성자
    public DirectoryPermission(Directory directory, Long deptId) {
        this.directory = directory;
        this.deptId = deptId;
    }
}