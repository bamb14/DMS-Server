package com.dms.demo.dto;

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
}
