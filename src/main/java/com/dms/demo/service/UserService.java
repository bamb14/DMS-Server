package com.dms.demo.service;

import com.dms.demo.dto.UserDto;
import com.dms.demo.entity.Department;
import com.dms.demo.entity.User;
import com.dms.demo.exception.DuplicateResourceException;
import com.dms.demo.exception.ResourceNotFoundException;
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

    // 유저 생성
    @Transactional
    public User createUser(UserDto.CreateRequest request) {
        // 1. 사번 중복 체크
        if (userRepository.existsByEmpNo(request.getEmpNo())) {
            throw new DuplicateResourceException("이미 존재하는 사번입니다: " + request.getEmpNo());
        }
        
        // 2. 부서명으로 부서 조회
        Department department = departmentRepository.findByDeptName(request.getDept())
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 부서입니다: " + request.getDept()));

        // 3. 유저 생성
        User newUser = User.builder()
                .empNo(request.getEmpNo())
                .name(request.getName())
                .passwordHash(request.getPasswordHash())
                .department(department)
                .build();
        return userRepository.save(newUser);
    }

    // 로그인
    @Transactional
    public UserDto.LoginResponse login(UserDto.LoginRequest req) {
    	User user=userRepository.findByEmpNo(req.empNo())
    			.orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));
    	
    	return UserDto.LoginResponse.from(user);
    }
}