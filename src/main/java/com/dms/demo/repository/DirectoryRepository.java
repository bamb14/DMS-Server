package com.dms.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.dms.demo.entity.Directory;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    
}
