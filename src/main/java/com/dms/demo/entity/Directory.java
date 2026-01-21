package com.dms.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "directories")
public class Directory {

    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dir_id")
    private Long dirId;

    @Column(nullable = false)
    private String name;

    private boolean isDeleted = false;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    // [부모 폴더] DB의 parent_id 컬럼과 매핑되는 '객체'
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true) 
    private Directory parent;

    // [자식 폴더들]
    @OneToMany(mappedBy = "parent")
    private List<Directory> children = new ArrayList<>();
    
    @Builder
    public Directory(String name, Directory parent, Long ownerId) {
        this.name = name;
        this.parent = parent;
        this.ownerId = ownerId;
        this.isDeleted = false;
    }
}