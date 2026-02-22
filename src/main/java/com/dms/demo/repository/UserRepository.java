package com.dms.demo.repository;

import com.dms.demo.entity.User;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
   boolean existsByEmpNo(String empNo);

	Optional<User> findByEmpNo(String empNo);
}