package com.dms.demo.service;

import com.dms.demo.entity.User;
import com.dms.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    // 1. 생성 (Create)
    @Transactional
    public User createUser(User user) {
        return userRepository.save(user);
    }

    // 4. 삭제 (Delete)
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}