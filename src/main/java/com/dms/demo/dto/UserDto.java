package com.dms.demo.dto;

import com.dms.demo.entity.User;

import lombok.Builder;
import lombok.Getter;

public final class UserDto {

    @Getter
    public static class CreateRequest {
        private String empNo;
        private String passwordHash;
        private String name;
        private String dept;
    }

    public record LoginRequest (
    	String empNo,
    	String passwordHash
    ) {}
	
	@Getter
	@Builder
	public static class LoginResponse{
		private Long id;
		private String name;
		private Long deptId;
		private String deptName;
		
		public static LoginResponse from(User user) {
			return LoginResponse.builder()
					.id(user.getUserId())
					.name(user.getName())
					.deptId(user.getDeptId())
					.deptName(user.getDepartment().getDeptName())
					.build();
			
		}
	}
}
