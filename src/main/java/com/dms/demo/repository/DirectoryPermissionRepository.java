package com.dms.demo.repository;

import com.dms.demo.entity.DirectoryPermission;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DirectoryPermissionRepository extends JpaRepository<DirectoryPermission, Long> {
	boolean existsByDirectory_DirIdAndDeptId(Long dirId, Long deptId);
}