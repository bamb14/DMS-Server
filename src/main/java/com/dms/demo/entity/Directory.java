package com.dms.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "directories")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Directory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "dir_id")
	private Long dirId;

	@ManyToOne(fetch = FetchType.LAZY) // 지연 로딩 (필수!)
    @JoinColumn(name = "parent_id")    // DB 컬럼명
    private Directory parent;

	@Column(name = "name", nullable = false, length = 255)
	private String name;

	@Column(name = "created_by", nullable = false)
	private Long ownerId;

	@CreationTimestamp
	@Column(name = "created_at", updatable = false)
	private LocalDateTime createdAt;

	@Builder.Default
	@Column(name = "is_deleted", nullable = false)
	private Boolean isDeleted = false;
	
	@BatchSize(size = 100)
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
    private List<Directory> children = new ArrayList<>();
	
	// [추가] 이 폴더에 걸린 권한들 (양방향 매핑)
    @OneToMany(mappedBy = "directory", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<DirectoryPermission> permissions = new ArrayList<>();
	
	public void rename(String newName) {
        this.name = newName;
    }
	
	public void delete() {
		this.isDeleted = true;
	}
	
	@Builder
    public Directory(String name, Directory parent, Long ownerId) {
        this.name = name;
        this.parent = parent;
        this.ownerId = ownerId;
        this.isDeleted = false;
    }
}