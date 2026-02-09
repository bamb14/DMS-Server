package com.dms.demo.service;

import com.dms.demo.dto.UserDto;
import com.dms.demo.entity.Department;
import com.dms.demo.entity.User;
import com.dms.demo.repository.DepartmentRepository;
import com.dms.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    // 1. 생성 (Create)
    @Transactional
    public User createUser(UserDto.CreateRequest req) {
        // 2. [추가] 부서명으로 부서 조회 (없으면 에러)
        Department department = departmentRepository.findByDeptName(req.getDeptName());

        if (department == null) {
            throw new RuntimeException("존재하지 않는 부서입니다: " + req.getDeptName());
        }

        String passwordHash = passwordEncoder.encode(req.getPasswordHash());

        // 3. 유저 생성 (부서 정보를 같이 넣어줌)
        User newUser = User.builder()
                .empNo(req.getEmpNo())
                .name(req.getName())
                .passwordHash(passwordHash)
                .department(department)
                .build();
        return userRepository.save(newUser);
    }

    // 4. 삭제 (Delete)
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
