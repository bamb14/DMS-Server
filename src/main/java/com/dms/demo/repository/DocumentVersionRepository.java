package com.dms.demo.repository;

import com.dms.demo.entity.DocumentVersion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentVersionRepository extends JpaRepository<DocumentVersion, Long> {
	List<DocumentVersion> findByDocId(Long docId);
	
}