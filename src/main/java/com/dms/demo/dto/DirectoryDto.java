package com.dms.demo.dto;

import lombok.Data;

public class DirectoryDto {

    // 1. 폴더 생성 요청용
    @Data
    public static class CreateRequest {
        private String name;
        private Long parentId; // 루트 폴더면 null 혹은 아예 안 보냄
        private Long ownerId;
    }

    // 2. 권한 부여 요청용
    @Data
    public static class GrantPermissionRequest {
        private Long deptId; // 어떤 부서에게 권한을 줄지
    }

    // 3. 이름 변경 요청용
    @Data
    public static class RenameRequest {
        private String newName;
    }
}