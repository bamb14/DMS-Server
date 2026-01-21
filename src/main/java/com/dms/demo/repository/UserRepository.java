package com.dms.demo.repository;

import com.dms.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // 아무것도 안 적어도 findById, save, delete 등은 이미 다 있습니다.
}