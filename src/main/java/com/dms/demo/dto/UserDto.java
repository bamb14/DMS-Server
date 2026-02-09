package com.dms.demo.dto;

import com.dms.demo.entity.User;

import lombok.Getter;
import lombok.Setter;

public final class UserDto {

    private UserDto() {} // 인스턴스화 방지

    @Getter @Setter
    public static class CreateRequest {
        private String empNo;
        private String passwordHash;
        private String name;
        private String deptName;
    }

    public record LoginRequest(
        String empNo,
        String passwordHash
    ){}

    @Getter
    @Builder
    public static class LoginResponse{
        private Long id;
        private String name;
        private Long deptId;
        private String deptName;

        public static LoginResponse from (User user){
            return LoginResponse.builder()
                    .id(user.getUserId())
                    .name(user.getName())
                    .deptId(user.getDeptId())
                    .deptName(user.getDepartment().getDeptName())
                    .build();
        }
    }
}
