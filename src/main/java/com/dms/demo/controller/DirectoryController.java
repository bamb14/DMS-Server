package com.dms.demo.controller;

import com.dms.demo.dto.DirectoryDto;
import com.dms.demo.dto.DirectoryDto.DirectoryResponseDto;
import com.dms.demo.entity.Directory;
import com.dms.demo.service.DirectoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/directories")
@RequiredArgsConstructor
public class DirectoryController {

    private final DirectoryService directoryService;

        // ==========================================
    // 1. 폴더 생성
    // POST /api/directories
    // Body: { "name": "기획팀 문서", "parentId": null }
    // ==========================================
    @PostMapping
    public ResponseEntity<Long> createDirectory(@RequestBody DirectoryDto.CreateRequest request) {
        Directory dir = directoryService.createDirectory(request.getUserId(), request.getName(), request.getParentId());
        return ResponseEntity.ok(dir.getDirId());
    }

    // ==========================================
    // 디렉토리 목록 조회 API
    // GET /api/directories?userId=1&deptId=10 (루트 폴더 조회)
    // GET /api/directories?parentId=5&userId=1&deptId=10 (하위 폴더 조회)
    // ==========================================
    @GetMapping
    public ResponseEntity<List<DirectoryResponseDto>> getDirectories(
    		@RequestParam(value = "parentId", required = false) Long parentId, // null이면 루트 조회
    		@RequestParam("userId") Long userId,
    		@RequestParam("deptId") Long deptId) {
    	
    	// 서비스 내부에서 로직 분기 처리
    	// 1. parentId == null : 루트 폴더 중 (내 소유 or 내 부서 권한) 필터링 조회
    	// 2. parentId != null : 상위 권한 재귀 체크 후 자식 폴더 전체 조회
    	List<DirectoryResponseDto> directories = directoryService.getDirectoryList(parentId, userId, deptId);
    	
    	return ResponseEntity.ok(directories);
    }


    // ==========================================
    // 5. 폴더 이름 변경
    // PUT /api/directories/{dirId}/rename
    // Body: { "newName": "변경된 이름" }
    // ==========================================
    @PutMapping("/{dirId}/rename")
    public ResponseEntity<String> renameDirectory(
            @PathVariable("dirId") Long dirId,
            @RequestParam("userId") Long userId,
            @RequestBody DirectoryDto.RenameRequest request) {
        
        directoryService.renameDirectory(dirId, userId, request.getNewName());
        return ResponseEntity.ok("이름 변경 완료");
    }

    // ==========================================
    // 6. 폴더 삭제
    // DELETE /api/directories/{dirId}
    // ==========================================
    @DeleteMapping("/{dirId}")
    public ResponseEntity<String> deleteDirectory(
            @PathVariable("dirId") Long dirId,
            @RequestParam("userId") Long userId
    ) {
        directoryService.deleteDirectory(dirId, userId);
        
        return ResponseEntity.ok("디렉토리가 성공적으로 삭제되었습니다.");
    }
}