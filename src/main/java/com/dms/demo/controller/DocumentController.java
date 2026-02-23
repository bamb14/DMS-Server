package com.dms.demo.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.dms.demo.dto.DocumentDto;
import com.dms.demo.entity.Document;
import com.dms.demo.exception.DuplicateResourceException;
import com.dms.demo.service.DocumentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    // POST /api/documents/upload
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upload(
    		@RequestParam("userId") Long userId,
            @RequestParam("dirId") Long dirId,
            @RequestParam("file") MultipartFile file,
            // required=false로 설정해서, 안 보내면 기본값 "CHECK"로 동작
            @RequestParam(value = "mode", defaultValue = "CHECK") String mode 
    ) {
        try {
            Document doc=documentService.uploadFile(userId, dirId, file, mode);
            return ResponseEntity.ok(doc.getDocId());
            
        } catch (DuplicateResourceException e) {
//             중복 발생 시 409 Conflict 리턴 -> 프론트에서 팝업 띄움
            return ResponseEntity.status(409).body("DUPLICATE_FILE");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("에러: " + e.getMessage());
        }
    }
    
    // GET /api/documents?dirId=1
    @GetMapping
    public ResponseEntity<List<DocumentDto.DocumentResponseDto>> getDocumentList(@RequestParam("dirId") Long dirId) {
    	List<DocumentDto.DocumentResponseDto> list = documentService.getDocumentList(dirId);
        return ResponseEntity.ok(list);
    }
    
    // POST /api/{docId}/lock
    @PostMapping("/{docId}/lock")
    public ResponseEntity<String> lockDocument(
    		@PathVariable("docId") Long docId,
    		@RequestParam("userId") Long userId,
            @RequestBody Map<String, String> body // "reason"을 JSON으로 받기 위함
    ) {
    
        String reason = body.get("reason");
        
        documentService.lockDocument(docId, userId, reason);
        
        return ResponseEntity.ok("파일이 성공적으로 잠겼습니다.");
    }

    // POST /api/{docId}/unlock
    @PostMapping("/{docId}/unlock")
    public ResponseEntity<String> unlockDocument(@PathVariable("docId") Long docId, @RequestParam("userId") Long userId) {
        
        documentService.unlockDocument(docId, userId);
        
        return ResponseEntity.ok("잠금이 해제되었습니다.");
    }
    
    // DELETE /api/{docId}?userId=
    @DeleteMapping("/{docId}")
    public ResponseEntity<String> deleteDocument(
            @PathVariable("docId") Long docId,
            @RequestParam("userId") Long userId
    ) {
        documentService.deleteDocument(docId, userId);
        
        return ResponseEntity.ok("파일이 성공적으로 삭제되었습니다.");
    }
    
    @PutMapping("/{docId}/rename")
    public ResponseEntity<String> renameDocument(
    		@PathVariable("docId") Long docId,
            @RequestParam("userId") Long userId,
            @RequestBody DocumentDto.RenameRequest request
    ){
    	documentService.renameDocument(docId, userId, request.getNewName());
    	return ResponseEntity.ok("이름 변경 완료");
    }
    
    @GetMapping("/{docId}/version")
    public ResponseEntity<List<DocumentDto.DocumentVersionDto>> getDocumentVersionList(
    		@PathVariable("docId") Long docId,
    		@RequestParam("userId") Long userId){
    	
    	List<DocumentDto.DocumentVersionDto> list=documentService.getDocumentVersionList(docId, userId);
    	
    	return ResponseEntity.ok(list);
    }
    
    @GetMapping("/download/{versionId}")
    public ResponseEntity<Map<String, String>> getDownloadUrl(
            @PathVariable("versionId") Long fileId,
            @RequestParam("userId") Long userId) {

        String url = documentService.getDocumentDownloadUrl(fileId, userId);
        
        Map<String, String> response = new HashMap<>();
        response.put("url", url);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/delete/version/{versionId}")
    public ResponseEntity<String> deleteDocumentVersion(
            @PathVariable("versionId") Long versionId,
            @RequestParam("userId") Long userId
    ) {
        documentService.deleteDocumentVersion(versionId, userId);
        
        return ResponseEntity.ok("파일 버전이 성공적으로 삭제되었습니다.");
    }
}