package com.dms.demo.repository;

import com.dms.demo.entity.Document;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

    List<Document> findAllByDirIdAndIsDeletedFalse(Long dirId);

    Optional<Document> findByDirIdAndDisplayNameAndIsDeletedFalse(Long dirId, String displayName);

	boolean existsByDirIdAndDisplayName(Long dirId, String newName);
}