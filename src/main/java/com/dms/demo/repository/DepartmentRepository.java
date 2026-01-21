package com.dms.demo.repository;

import com.dms.demo.entity.Department;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {
    // 부서 이름으로 부서 찾기
    Department findByDeptName(String deptName);
}