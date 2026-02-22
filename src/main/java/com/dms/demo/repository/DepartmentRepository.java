package com.dms.demo.repository;

import com.dms.demo.entity.Department;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    boolean existsByDeptName(String deptName);
	
    Optional<Department> findByDeptName(String deptName);
}