package com.dms.demo.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.dms.demo.entity.Directory;
import com.dms.demo.repository.DirectoryRepository;

import lombok.RequiredArgsConstructor;


@Service
@RequiredArgsConstructor
public class DirectoryService {
    private final DirectoryRepository directoryRepository;

    @Transactional
    public Directory createDirectory(String name, Long parentId, Long ownerId) {
        
        Directory parentDir = null;
        if (parentId != null) {
            parentDir = directoryRepository.findById(parentId)
                    .orElseThrow(() -> new IllegalArgumentException("상위 폴더가 존재하지 않습니다."));
        }

        Directory directory = Directory.builder()
                .name(name)
                .ownerId(ownerId)
                .parent(parentDir) // null이면 루트 디렉토리
                .build();        

        return directoryRepository.save(directory);
    }
}
