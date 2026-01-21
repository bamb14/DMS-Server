package com.dms.demo.service;

import com.dms.demo.dto.UserDto;
import com.dms.demo.entity.Department;
import com.dms.demo.entity.User;
import com.dms.demo.repository.DepartmentRepository;
import com.dms.demo.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;

    // 1. 생성 (Create)
    @Transactional
    public User createUser(UserDto.CreateRequest req) {
        // 2. [추가] 부서명으로 부서 조회 (없으면 에러)
        Department department = departmentRepository.findByDeptName(req.getDeptName());

        if (department == null) {
            throw new RuntimeException("존재하지 않는 부서입니다: " + req.getDeptName());
        }

        // 3. 유저 생성 (부서 정보를 같이 넣어줌)
        User newUser = User.builder()
                .empNo(req.getEmpNo())
                .name(req.getName())
                .passwordHash(req.getPasswordHash())
                .department(department) // 👈 찾아낸 부서 객체(Entity)를 통째로 넣어줍니다 (JPA 연관관계)
                .build();
        return userRepository.save(newUser);
    }

    // 4. 삭제 (Delete)
    @Transactional
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}