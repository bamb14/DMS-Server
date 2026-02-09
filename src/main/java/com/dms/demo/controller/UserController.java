package com.dms.demo.controller;

import com.dms.demo.dto.UserDto;
import com.dms.demo.entity.User;
import com.dms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/api/users") // 이 컨트롤러의 기본 주소
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public User createUser(@RequestBody UserDto.CreateRequest req) {
        return userService.createUser(req);
    }

    @PostMapping("/login")
    public ResponseEntity<UserDto.LoginResponse> login(@RequestBody UserDto.LoginRequest request) {
        UserDto.LoginResponse response = userService.login(request);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id)") Long id) {
        userService.deleteUser(id);
    }
}
