package com.dms.demo.service;

import com.dms.demo.dto.DocumentDto;
import com.dms.demo.entity.Directory;
import com.dms.demo.entity.DirectoryPermission;
import com.dms.demo.entity.Document;
import com.dms.demo.entity.DocumentLock;
import com.dms.demo.entity.DocumentVersion;
import com.dms.demo.entity.User;
import com.dms.demo.exception.DuplicateResourceException;
import com.dms.demo.exception.ResourceNotFoundException;
import com.dms.demo.repository.DirectoryRepository;
import com.dms.demo.repository.DocumentLockRepository;
import com.dms.demo.repository.DocumentRepository;
import com.dms.demo.repository.DocumentVersionRepository;
import com.dms.demo.repository.UserRepository;
import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class DocumentService {
    private final DocumentRepository documentRepository;
    private final DocumentVersionRepository versionRepository;
    private final DocumentLockRepository lockRepository;
    private final UserRepository userRepository;
    private final DirectoryRepository directoryRepository;
    private final MinioStorageService minioStorageService;
    
    @Value("${minio.bucket-name}")
    private String bucketName;
    
    @Transactional
    public Document uploadFile(Long userId, Long dirId, MultipartFile file, String mode) {
        String originalFilename = file.getOriginalFilename();
        
        Directory directory = directoryRepository.findById(dirId)
                .orElseThrow(() -> new ResourceNotFoundException("폴더를 찾을 수 없습니다: " + dirId));
        
        // 1. 중복 파일 검색
        Optional<Document> existingDocOpt = documentRepository.findByDirIdAndDisplayNameAndIsDeletedFalse(dirId, originalFilename);

        // 2. 중복된 경우 처리 로직
        if (existingDocOpt.isPresent()) {
            Document existingDoc = existingDocOpt.get();

            if ("CHECK".equalsIgnoreCase(mode)) {
                // A. 기본 모드: 중복 발견 시 에러 발생 -> 클라이언트가 잡아서 팝업 띄움
                throw new DuplicateResourceException("동일한 이름의 파일이 존재합니다.");
            
            } else if ("OVERWRITE".equalsIgnoreCase(mode)) {
                // B. 덮어쓰기 (버전 업)
            	checkWritePermission(existingDoc, userId);
            	
                createNewVersion(existingDoc, file, userId);
                return existingDoc; // 종료
            
            } else if ("NEW".equalsIgnoreCase(mode)) {
                // C. 새 파일로 저장 (이름 변경 로직 필요: 파일명(1).pdf)
                originalFilename = renameDuplicateFile(dirId, originalFilename);
            }
        }

        // 3. 신규 저장 (중복이 없거나, 모드가 NEW여서 이름이 바뀐 경우)
        Document doc = createNewDocument(dirId, file, originalFilename, userId);
        return doc;
    }

    // [내부 메소드 1] 신규 문서 생성
    private Document createNewDocument(Long dirId, MultipartFile file, String filename, Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String ext = getExtension(filename);
            String storageKey = now.toLocalDate().toString() + "/" + UUID.randomUUID();

            // MinIO 업로드
            minioStorageService.uploadToMinio(file, storageKey);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저 없음"));

            // Document 저장
            Document doc = Document.builder()
                    .dirId(dirId)
                    .displayName(filename) // (중복 시 변경된 이름일 수 있음)
                    .originalName(file.getOriginalFilename()) // 실제 원본 이름
                    .normalizedName(filename.toLowerCase())
                    .fileExt(ext)
                    .createdBy(userId)
                    .createdAt(now)
                    .updatedAt(now)
                    .updatedBy(userId)
                    .updatedByName(user.getName())
                    .latestVersionNo(1)                  
                    .latestFileSize(file.getSize())
                    .isDeleted(false)
                    .build();
            Document savedDoc = documentRepository.save(doc);

            // Version 저장 (1버전)
            DocumentVersion newVersion = saveVersion(savedDoc.getDocId(), savedDoc.getDisplayName(), 1, storageKey, file.getSize(), userId);
            savedDoc.updateLatestVersionId(newVersion.getDocVersionId());
            return savedDoc;
          
        } catch (Exception e) {
            throw new RuntimeException("신규 업로드 실패: " + e.getMessage());
        }
    }

    // [내부 메소드 2] 버전 업 (덮어쓰기)
    private void createNewVersion(Document existingDoc, MultipartFile file, Long userId) {
        try {
            LocalDateTime now = LocalDateTime.now();
            String filename = existingDoc.getOriginalName();
            String storageKey = now.toLocalDate().toString() + "/" + UUID.randomUUID();

            // MinIO 업로드
            minioStorageService.uploadToMinio(file, storageKey);

            // Document 업데이트 (최신 버전 번호 증가, 수정시간 갱신)
            int newVersionNo = existingDoc.getLatestVersionNo() + 1;
            
            // Version 테이블에 새 줄 추가
            DocumentVersion newVersion=saveVersion(existingDoc.getDocId(), existingDoc.getDisplayName(),newVersionNo, storageKey, file.getSize(), userId);
            
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("유저 없음"));
            
            existingDoc.updateLatestVersionInfo(newVersion, userId, user.getName());


        } catch (Exception e) {
            throw new RuntimeException("버전 업로드 실패: " + e.getMessage());
        }
    }
    
    // [추가] 문서 목록 조회
    @Transactional(readOnly = true) // 조회 전용이므로 성능 최적화
    public List<DocumentDto.DocumentResponseDto> getDocumentList(Long dirId) {
        
        if(!directoryRepository.existsById(dirId)) {
        	throw new ResourceNotFoundException("존재하지 않는 폴더입니다.");
        }

        // B. 자격이 있다면 DB에서 단순 조회
        List<Document> documents = documentRepository.findAllByDirIdAndIsDeletedFalse(dirId);
        if (documents.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 2. 잠금 정보(Lock) 일괄 조회 (쿼리 2번: select * from document_lock where doc_id in (...))
        //    - 문서 ID들을 리스트로 추출
        List<Long> docIds = documents.stream()
                .map(Document::getDocId)
                .collect(Collectors.toList());
        // - 해당 문서들에 걸린 락을 전부 가져와서 Map으로 변환 (Key: docId, Value: Lock객체)
        List<DocumentLock> locks = lockRepository.findAllByDocIdIn(docIds);
        Map<Long, DocumentLock> lockMap = locks.stream()
                .collect(Collectors.toMap(DocumentLock::getDocId, lock -> lock));

        // 3. User ID 수집 (수정자 + 잠금자) - 중복 제거
        Set<Long> userIds = new HashSet<>();
        
        // 3-1. 문서 수정자 ID 수집
        documents.forEach(doc -> {
            if (doc.getUpdatedBy() != null) userIds.add(doc.getUpdatedBy());
        });
        
        // 3-2. 잠금 건 유저(Locker) ID 수집
        locks.forEach(lock -> {
            if (lock.getLockedBy() != null) userIds.add(lock.getLockedBy());
        });

        // 4. User 일괄 조회 (쿼리 3번: select * from user where id in (...))
        Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));

        // 5. DTO 변환 및 매핑
        return documents.stream()
                .map(document -> {
                    // A. 수정자 이름 찾기
                    String updatedByName = userNameMap.getOrDefault(document.getUpdatedBy(), "알 수 없음");

                    // B. 락 정보 찾기 (Map에서 docId로 조회)
                    DocumentLock lock = lockMap.get(document.getDocId());
                    
                    boolean isLocked = (lock != null);
                    String lockReason = null;
                    Long lockedBy=null;

                    if (isLocked) {
                        lockReason = lock.getLockReason();
                        lockedBy=lock.getLockedBy();
                    }

                    // DTO 생성 (생성자 파라미터 순서에 주의하세요)
                    return new DocumentDto.DocumentResponseDto(
                            document, 
                            document.getUpdatedBy(),
                            updatedByName, 
                            isLocked, 
                            lockReason, 
                            lockedBy
                    );
                })
                .collect(Collectors.toList());
    }
    
    // "이 파일 지금 건드려도 돼?" 확인하는 메소드
    private void checkWritePermission(Document doc, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유저 없음"));
        
        // [변경] 객체 그래프 탐색으로 ID 가져오기
        Long userDeptId = user.getDepartment().getDeptId();

        // 3. 권한 체크 (내 ID + 내 부서 ID 모두 전달)
        // owner면 통과, 아니면 부서 권한 확인
        boolean hasAccess = directoryRepository.hasAccessPermission(
                                doc.getDirId(), 
                                userId, 
                                userDeptId 
                            );
        
        if (!hasAccess) {
             throw new RuntimeException("소속 부서(" + user.getDepartment().getDeptName() + ")에 접근 권한이 없습니다.");
        }
        
        // 2. [Lock 체크] 잠금 상태 확인 (기존 로직)
        lockRepository.findById(doc.getDocId()).ifPresent(lock -> {
            // 만료 기간 지났으면 잠금 삭제 후 통과
            if (lock.getLockExpiresAt() != null && lock.getLockExpiresAt().isBefore(LocalDateTime.now())) {
                lockRepository.delete(lock);
                return;
            }
            
            // 내가 잠근 게 아니면 에러 발생
            if (!lock.getLockedBy().equals(userId)) {
                throw new RuntimeException("이 파일은 현재 편집 잠금 상태입니다. (잠근 사용자 ID: " + lock.getLockedBy() + ")");
            }
        });
    }
    
    @Transactional
    public void lockDocument(Long docId, Long userId, String reason) {
    	Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다."));
    	
        // A. 권한 체크: 나한테 'Lock' 권한이 있는가?
    	User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저가 존재하지 않습니다."));

        // 2. 권한 체크 (핵심 로직 분리)
        if (!hasLockPermission(document.getDirId(), user)) {
            throw new AccessDeniedException("문서를 잠글 권한이 없습니다.");
        }

        // B. 이미 잠겨있는지 확인
        if (lockRepository.existsById(docId)) {
            throw new RuntimeException("이미 잠겨있는 파일입니다."); // 혹은 만료기간 체크 후 덮어쓰기 로직
        }

        // C. 잠금 생성 (1주일)
        DocumentLock lock = DocumentLock.builder()
                .docId(docId)
                .lockedBy(userId)
                .lockedAt(LocalDateTime.now())
                .lockExpiresAt(LocalDateTime.now().plusWeeks(1)) // 1주일 후 만료
                .lockReason(reason)
                .build();

        lockRepository.save(lock);
    }
    
    private boolean hasLockPermission(Long dirId, User user) {
        Long userDeptId = (user.getDeptId() != null) ? user.getDeptId() : null;
        Directory currentDir = directoryRepository.findById(dirId)
                .orElseThrow(() -> new IllegalArgumentException("디렉토리를 찾을 수 없습니다."));

        while (currentDir != null) {
            // A. 디렉토리 소유자가 '나'인 경우 -> 권한 있음 (Pass)
            if (currentDir.getOwnerId().equals(user.getUserId())) {
                return true;
            }

            // B. 디렉토리 권한 목록에 '내 부서'가 포함된 경우 -> 권한 있음 (Pass)
            if (userDeptId != null && hasDepartmentPermission(currentDir.getPermissions(), userDeptId)) {
                return true;
            }

            // C. 권한이 없다면 상위 디렉토리로 이동 (부모가 없으면 null이 되어 루프 종료)
            currentDir = currentDir.getParent();
        }

        // 루트까지 갔는데도 권한이 없으면 False
        return false;
    }

    // 디렉토리의 permission 리스트에서 내 부서 ID가 있는지 확인
    private boolean hasDepartmentPermission(List<DirectoryPermission> directoryPermissions, Long userDeptId) {
        // Directory 엔티티가 List<DirectoryPermission> permissions를 가지고 있다고 가정
        return directoryPermissions.stream()
                .anyMatch(permission -> permission.getDeptId().equals(userDeptId));
    }

    // 2. 잠금 해제
    @Transactional
    public void unlockDocument(Long docId, Long userId) {
    	if(!documentRepository.existsById(docId)) {
    		throw new ResourceNotFoundException("존재하지 않는 문서입니다.");
    	}
    	
        DocumentLock lock = lockRepository.findById(docId)
                .orElseThrow(() -> new RuntimeException("잠겨있지 않은 파일입니다."));

        // 본인만 해제 가능 (또는 관리자)
        if (!lock.getLockedBy().equals(userId)) {
             throw new RuntimeException("파일을 잠근 사용자만 해제할 수 있습니다.");
        }

        lockRepository.delete(lock);
    }
    
    // 문서 삭제
    @Transactional
    public void deleteDocument(Long docId, Long userId) {
    	Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다."));
    	
    	if (lockRepository.existsById(docId)) {
            throw new IllegalStateException("해당 문서는 현재 잠겨 있어 삭제할 수 없습니다.");
        }
    	
    	if (!document.getUpdatedBy().equals(userId)) {
            throw new AccessDeniedException("문서를 삭제할 권한이 없습니다.");
        }
    	
    	document.delete();
    }
    
    // 문서명 수정
    @Transactional
    public void renameDocument(Long docId, Long userId, String newName) {

    	Document document = documentRepository.findById(docId)
                .orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다."));
    	
    	if (lockRepository.existsById(docId)) {
            throw new IllegalStateException("해당 문서는 현재 잠겨 있어 수정할 수 없습니다.");
        }
    	
    	if (!document.getUpdatedBy().equals(userId)) {
            throw new AccessDeniedException("문서를 수정할 권한이 없습니다.");
        }

        // [중복 검사] 생성하기 전에 이름 체크!
        checkDuplicateName(document.getDirId(), newName);
  
        document.rename(newName);
        DocumentVersion version = versionRepository.findById(document.getLatestVersionId())
        		.orElseThrow(() -> new IllegalArgumentException("문서 버전이 존재하지 않습니다."));
        version.rename(newName);
    }
    
    // 문서 버전 이력 조회
    public List<DocumentDto.DocumentVersionDto> getDocumentVersionList(Long docId, Long userId){
    	Document document = documentRepository.findById(docId)
    			.orElseThrow(() -> new IllegalArgumentException("문서가 존재하지 않습니다."));

    	List<DocumentVersion> list=versionRepository.findByDocId(docId);
    	list.sort(Comparator.comparing(DocumentVersion::getCreatedAt).reversed());
    	
    	if(list.isEmpty()) {
    		return new ArrayList<>();
    	}
    	
    	Set<Long> userIds = new HashSet<>();
    	
    	list.forEach(version -> {
            if (version.getUploadedBy() != null) userIds.add(version.getUploadedBy());
        });
    	
    	Map<Long, String> userNameMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(User::getUserId, User::getName));
    	
    	return list.stream().map(version -> {
    		String updatedByName = userNameMap.getOrDefault(version.getUploadedBy(), "알 수 없음");
    		
    		return new DocumentDto.DocumentVersionDto(
    				version,
                    document.getFileExt(),
                    updatedByName
            );
    	}).collect(Collectors.toList());
    }
    
    public void deleteDocumentVersion(Long versionId, Long userId) {
    	versionRepository.deleteById(versionId);
    }

    // [유틸] 중복 시 이름 변경 로직 (ex: 파일(1).txt)
    private String renameDuplicateFile(Long dirId, String originalFilename) {
        String name = originalFilename;
        String ext = "";
        int dotIndex = originalFilename.lastIndexOf('.');
        
        if (dotIndex != -1) {
            name = originalFilename.substring(0, dotIndex);
            ext = originalFilename.substring(dotIndex); // .txt 포함
        }

        int count = 1;
        String newName = originalFilename;
        
        // 중복이 없을 때까지 (1), (2) 증가
        while (documentRepository.findByDirIdAndDisplayNameAndIsDeletedFalse(dirId, newName).isPresent()) {
            newName = name + " (" + count + ")" + ext;
            count++;
        }
        return newName;
    }


    // [유틸] 버전 저장 공통화
    private DocumentVersion saveVersion(Long docId, String fileName, Integer versionNo, String storageKey, Long size, Long userId) {
        DocumentVersion version = DocumentVersion.builder()
                .docId(docId)
                .versionNo(versionNo)
                .versionFileName(fileName)
                .storageKey(storageKey)
                .sizeBytes(size)
                .uploadedBy(userId)
                .createdAt(LocalDateTime.now())
                .build();
        versionRepository.save(version);
        return version;
    }
	
	 // [유틸] 확장자 추출 메소드
	 private String getExtension(String filename) {
	     if (filename == null || filename.lastIndexOf('.') == -1) {
	         return "";
	     }
	     return filename.substring(filename.lastIndexOf('.') + 1);
	 }
	 
	 
	 private void checkDuplicateName(Long dirId, String newName) {
		 if(documentRepository.existsByDirIdAndDisplayName(dirId, newName)) {
			 throw new DuplicateResourceException("이미 동일한 이름의 문서가 존재합니다:" + newName);
		 }
	 }
	 
	 public String getDocumentDownloadUrl(Long versionId, Long userId) {
	        // 1. 파일 조회 및 권한 검증
//	        FileEntity file = validateAndGetFile(fileId, userId, deptId);
		 DocumentVersion file = versionRepository.findById(versionId)
				 .orElseThrow(() -> new IllegalArgumentException("해당 버전을 찾을 수 없습니다."));
		 
		 Document doc = documentRepository.findById(file.getDocId())
				 .orElseThrow(() -> new IllegalArgumentException("문서를 찾을 수 없습니다."));
		 
	        // 2. MinIO에서 임시 URL 생성
	        return minioStorageService.getDownloadUrl(file.getStorageKey(), file.getVersionFileName());
	    }
}