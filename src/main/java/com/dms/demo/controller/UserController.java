package com.dms.demo.controller;

import com.dms.demo.dto.UserDto;
import com.dms.demo.entity.User;
import com.dms.demo.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users") // 이 컨트롤러의 기본 주소
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // POST /api/users
    @PostMapping
    public User create(@RequestBody UserDto.CreateRequest user) {
        return userService.createUser(user);
    }
    
    // POST /api/users/logins
    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(@RequestBody UserDto.LoginRequest req) {
    	UserDto.LoginResponse response = userService.login(req);
    	return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
