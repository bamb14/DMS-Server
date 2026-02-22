package com.dms.demo.dto;

import java.util.List;
import java.util.stream.Collectors;

import com.dms.demo.entity.Directory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;

public class DirectoryDto {

    // 1. 폴더 생성 요청용
    @Data
    public static class CreateRequest {
        private String name;
        private Long parentId; // 루트 폴더면 null 혹은 아예 안 보냄
        private Long userId;
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

    @Getter
    @Builder
    @AllArgsConstructor
    public static class DirectoryResponseDto {
        private Long dirId;
        private String name;
        private Long ownerId;
        private Long parentId; // 부모 ID만 포함 (전체 객체 X)
        private List<DirectoryResponseDto> children;

        public DirectoryResponseDto(Directory directory) {
            this.dirId = directory.getDirId();
            this.name = directory.getName();
            this.ownerId = directory.getOwnerId();
            // 부모가 null일 경우(최상위 폴더) 처리
            this.parentId = (directory.getParent() != null) ? directory.getParent().getDirId() : null;
            
            // 자식 엔티티들을 DTO로 변환하여 리스트에 담음 (재귀 호출 발생)
            if (directory.getChildren() != null && !directory.getChildren().isEmpty()) {
                this.children = directory.getChildren().stream()
                        // 1. 삭제된 폴더 제외 (Entity에 @Where가 없다면 여기서 걸러야 함)
                        .filter(child -> !child.getIsDeleted())
                        // 2. 자식 Directory -> 자식 DTO 변환 (여기서 재귀적으로 계속 내려감)
                        .map(DirectoryResponseDto::new)      
                        .collect(Collectors.toList());
            }else {
                this.children = null; // 자식 없으면 null 또는 빈 리스트
            }
        }

    }
}