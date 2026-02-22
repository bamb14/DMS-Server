package com.dms.demo.service;

import com.dms.demo.dto.DirectoryDto.DirectoryResponseDto;
import com.dms.demo.entity.Directory;
import com.dms.demo.entity.User;
import com.dms.demo.exception.DuplicateResourceException;
import com.dms.demo.exception.ResourceNotFoundException;
import com.dms.demo.repository.DirectoryPermissionRepository;
import com.dms.demo.repository.DirectoryRepository;
import com.dms.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DirectoryService {

    private final DirectoryRepository directoryRepository;
    private final DirectoryPermissionRepository directoryPermissionRepository;
    private final UserRepository userRepository;

    // ==================================================================
    // 1. 폴더 생성 (루트 폴더 & 하위 폴더 겸용)
    // ==================================================================
    @Transactional
    public Directory createDirectory(Long userId, String dirName, Long parentId) {        
        // [중복 검사] 생성하기 전에 이름 체크!
        checkDuplicateName(parentId, dirName);

        Directory parentDir = null;
        if (parentId != null) {
            // 부모 객체를 찾아옴 (실제 쿼리는 안 날아가고 프록시만 가져오는 getReferenceById 추천)
            parentDir = directoryRepository.findById(parentId)
                .orElseThrow(() -> new ResourceNotFoundException("부모 폴더 없음"));
        }

        Directory newDir = Directory.builder()
                .name(dirName)
                .parent(parentDir)
                .ownerId(userId)
                .build();
        
        return directoryRepository.save(newDir);
    }
    

    // ==================================================================
    // 5. 폴더 이름 변경
    // ==================================================================
    @Transactional
    public void renameDirectory(Long dirId, Long userId, String newName) {

        // 권한 체크
    	boolean hasAccess = directoryRepository.existsByDirIdAndOwnerId(dirId, userId);

        if (!hasAccess) {
            // 예외를 던져서 Controller에서 403 Forbidden 등으로 처리하게 함
            throw new RuntimeException("해당 폴더에 대한 이름 변경 권한이 없습니다."); 
        }

        Directory directory = directoryRepository.findByDirIdAndIsDeletedFalse(dirId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 폴더입니다."));
        
        // [중복 검사] 생성하기 전에 이름 체크!
        Long parendId = (directory.getParent() != null) ? directory.getParent().getDirId() : null;
        checkDuplicateName(parendId, newName);
  
        directory.rename(newName);
    }

    // ==================================================================
    // 6. 폴더 삭제
    // 하위 폴더까지 모두 삭제함. 단, 하위 폴더 중 하나라도 내 소유가 아닌 것이 발견되면 전체 롤백(Rollback)
    // ==================================================================
    @Transactional // RuntimeException 발생 시 자동으로 모든 변경사항 롤백
    public void deleteDirectory(Long dirId, Long userId) {
        // 1. 삭제할 루트 디렉토리 조회
        Directory rootDirectory = directoryRepository.findByDirIdAndIsDeletedFalse(dirId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 폴더입니다."));

        // 2. 재귀 삭제 실행 (내부에서 권한 체크 수행)
        validateAndMarkDeleted(rootDirectory, userId);
    }

    /**
     * 재귀적으로 삭제 처리 및 검증
     */
    private void validateAndMarkDeleted(Directory directory, Long userId) {
        // [중요] 현재 처리하려는 폴더가 내 소유인지 확인
        if (!directory.getOwnerId().equals(userId)) {
            // 예외를 던지면 @Transactional에 의해 지금까지의 모든 삭제 처리가 취소(Rollback)됨
            throw new IllegalArgumentException(
                String.format("삭제 실패: 하위 폴더 '%s'의 소유자가 본인이 아닙니다.", directory.getName())
            );
        }

        // 소유자 검증 통과 -> 삭제 처리 (isDeleted = true)
        directory.delete();

        // 자식 폴더들 순회
        if (directory.getChildren() != null) {
            for (Directory child : directory.getChildren()) {
                // 이미 삭제된 폴더는 건너뜀 (중복 처리 방지)
                if (!child.getIsDeleted()) {
                    // 재귀 호출
                    validateAndMarkDeleted(child, userId);
                }
            }
        }
    }

    // ==================================================================
    // 🔒 [내부 메소드] 권한 체크 (재귀 쿼리 사용)
    // ==================================================================
    private void checkAccessPermission(Long dirId, Long userId) {
        // 1. 유저의 부서 정보 가져오기
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저 정보 오류"));
        
        Long userDeptId = (user.getDepartment() != null) ? user.getDepartment().getDeptId() : null;

        // 2. Repository의 재귀 쿼리 호출 (상속된 권한까지 검사)
        boolean hasAccess = directoryRepository.hasAccessPermission(dirId, userId, userDeptId);

        if (!hasAccess) {
            // 예외를 던져서 Controller에서 403 Forbidden 등으로 처리하게 함
            throw new RuntimeException("해당 폴더에 대한 접근 권한이 없습니다."); 
        }
    }
    
    // ==================================================================
    // 🔍 [내부 메소드] 중복 이름 체크 로직 분리
    // ==================================================================
    private void checkDuplicateName(Long parentId, String dirName) {
        if (directoryRepository.existsByParentIdAndName(parentId, dirName)) {
            throw new DuplicateResourceException("이미 동일한 이름의 폴더가 존재합니다: " + dirName);
        }
    }
    
    // ==================================================================
    // 2. 하위 디렉토리 조회 (루트 폴더 & 하위 폴더 겸용)
    // ==================================================================
    public List<DirectoryResponseDto> getDirectoryList(Long parentId, Long userId, Long deptId) {
        
        List<Directory> directories;

        if (parentId == null) {
            // 1. 루트 폴더: DB 쿼리 단계에서 (OwnerId OR DeptId) 필터링
            directories = directoryRepository.findPermittedRootDirectories(userId, deptId);
        } 
        else {
            // 2. 하위 폴더: 상위 폴더들 중 하나라도 권한이 있는지 재귀 체크
            if (!hasAccessRecursively(parentId, userId, deptId)) {
                throw new IllegalArgumentException("접근 권한이 없습니다.");
            }
            // 권한 있으면 해당 폴더의 자식들 전부 반환 (상속)
            directories = directoryRepository.findAllByParent_DirIdAndIsDeletedFalse(parentId);
        }
        directories.sort(getPriorityComparator(userId));

        return directories.stream()
        		.map(dir -> toDto(dir, userId))
                .collect(Collectors.toList());
    }
    
    private DirectoryResponseDto toDto(Directory directory, Long userId) {
        // 1. 자식 폴더들을 DTO로 변환하기 전에 '정렬'부터 수행
        List<DirectoryResponseDto> sortedChildren = directory.getChildren().stream()
        		.filter(child -> !child.getIsDeleted())
                // 🚨 여기서 정렬! (내꺼 우선 -> 생성일 순)
                .sorted(getPriorityComparator(userId))
                // 🔄 재귀 호출 (자식의 자식도 정렬하기 위해 toDto를 다시 부름)
                .map(child -> toDto(child, userId)) 
                .collect(Collectors.toList());

        // 2. 현재 폴더 DTO 생성
        return DirectoryResponseDto.builder()
                .dirId(directory.getDirId())
                .name(directory.getName())
                .ownerId(directory.getOwnerId())
                .parentId((directory.getParent() != null) ? directory.getParent().getDirId() : null)
                .children(sortedChildren) // ✅ 정렬된 자식 리스트를 넣음
                .build();
    }
    
    private Comparator<Directory> getPriorityComparator(Long userId) {
        return Comparator.comparing((Directory d) -> {
            // [1순위] 내꺼(ownerId == userId)면 0(상위), 아니면 1(하위)
            return d.getOwnerId().equals(userId) ? 0 : 1;
        })
        .thenComparing(Directory::getCreatedAt); // [2순위] 생성일 순
    }

    /**
     * [수정됨] 권한 재귀 검사
     * 조건: 현재 폴더부터 루트까지 올라가면서 (Owner == me) OR (Permission has myDept) 확인
     */
    private boolean hasAccessRecursively(Long dirId, Long userId, Long deptId) {
        
        Directory current = directoryRepository.findById(dirId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 폴더입니다."));

        while (current != null) {
            // Check 1: 내가 생성자인가? (Directory 테이블의 ownerId)
            if (current.getOwnerId().equals(userId)) {
                return true;
            }

            // Check 2: 내 부서에 권한이 부여되었는가? (Permission 테이블)
            boolean hasDeptPerm = directoryPermissionRepository.existsByDirectory_DirIdAndDeptId(
                    current.getDirId(), deptId);
            
            if (hasDeptPerm) {
                return true;
            }

            // 현재 폴더에 권한 없으면 부모로 이동 (상위 폴더 권한 상속 확인)
            current = current.getParent();
        }

        // 끝까지 올라갔는데 권한이 없으면 False
        return false;
    }
}