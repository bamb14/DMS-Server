package com.dms.demo.controller;

import com.dms.demo.dto.DirectoryDto;
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
    // Body: { "name": "기획팀 문서", "parentId": null, "ownerId": 1 }
    // ==========================================
    @PostMapping
    public ResponseEntity<String> createDirectory(@RequestBody DirectoryDto.CreateRequest request) {
        Directory dir = directoryService.createDirectory(request.getName(), request.getParentId(), request.getOwnerId());
        return ResponseEntity.ok("폴더 생성 완료: " + dir.getDirId());
    }

    // ==========================================
    // 2. 루트 폴더 목록 조회 (최상위)
    // GET /api/directories/root
    // ==========================================
    // @GetMapping("/root")
    // public ResponseEntity<List<Directory>> getRootDirectories() {
    //     List<Directory> roots = directoryService.getRootDirectories();
    //     return ResponseEntity.ok(roots);
    // }

    // ==========================================
    // 3. 하위 폴더 목록 조회 (특정 폴더 클릭 시)
    // GET /api/directories/{dirId}/children
    // ==========================================
    // @GetMapping("/{dirId}/children")
    // public ResponseEntity<List<Directory>> getSubDirectories(@PathVariable Long dirId) {
    //     List<Directory> children = directoryService.getSubDirectories(dirId);
    //     return ResponseEntity.ok(children);
    // }

    // ==========================================
    // 4. 부서 권한 부여
    // POST /api/directories/{dirId}/permissions
    // Body: { "deptId": 1 }
    // ==========================================
    // @PostMapping("/{dirId}/permissions")
    // public ResponseEntity<String> grantPermission(
    //         @PathVariable Long dirId,
    //         @RequestBody DirectoryDto.GrantPermissionRequest request) {
        
    //     directoryService.grantPermissionToDepartment(dirId, request.getDeptId());
    //     return ResponseEntity.ok("권한 부여 완료");
    // }

    // ==========================================
    // 5. 폴더 이름 변경
    // PUT /api/directories/{dirId}/name
    // Body: { "newName": "변경된 이름" }
    // ==========================================
    // @PutMapping("/{dirId}/name")
    // public ResponseEntity<String> renameDirectory(
    //         @PathVariable Long dirId,
    //         @RequestBody DirectoryDto.RenameRequest request) {
        
    //     directoryService.renameDirectory(dirId, request.getNewName());
    //     return ResponseEntity.ok("이름 변경 완료");
    // }

    // ==========================================
    // 6. 폴더 삭제
    // DELETE /api/directories/{dirId}
    // ==========================================
    // @DeleteMapping("/{dirId}")
    // public ResponseEntity<String> deleteDirectory(@PathVariable Long dirId) {
    //     directoryService.deleteDirectory(dirId);
    //     return ResponseEntity.ok("폴더 삭제 완료");
    // }
}