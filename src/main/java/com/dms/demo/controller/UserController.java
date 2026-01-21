package com.dms.demo.controller;

import com.dms.demo.entity.User;
import com.dms.demo.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users") // 이 컨트롤러의 기본 주소
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 1. 생성 (Create)
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    // 4. 삭제 (Delete)
    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable("id") Long id) {
        userService.deleteUser(id);
    }
}
