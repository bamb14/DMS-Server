package com.dms.demo.repository;

import com.dms.demo.entity.DocumentLock;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentLockRepository extends JpaRepository<DocumentLock, Long> {

	List<DocumentLock> findAllByDocIdIn(List<Long> docIds);
}