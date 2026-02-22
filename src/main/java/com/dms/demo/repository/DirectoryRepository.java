package com.dms.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dms.demo.entity.Directory;

public interface DirectoryRepository extends JpaRepository<Directory, Long> {
    	/**
     * [접근 권한 체크]
     * 조건 1: 내가 주인(ownerId) 이거나
     * 조건 2: 내 부서(deptId)가 권한 테이블(permissions)에 있거나
     */
    @Query("SELECT COUNT(d) > 0 FROM Directory d " +
           "LEFT JOIN d.permissions p " +             // 권한 테이블과 조인
           "WHERE d.dirId = :dirId " +                // 1. 해당 폴더에 대해
           "AND (" +
           "   d.ownerId = :userId " +                // 2-1. 내가 주인이거나
           "   OR " +
           "   p.deptId = :deptId " +                 // 2-2. 내 부서 아이디가 권한 목록에 있거나
           ")")
    boolean hasAccessPermission(
            @Param("dirId") Long dirId, 
            @Param("userId") Long userId, 
            @Param("deptId") Long deptId
    );
	
	
	// ✅ 1. 중복 검사 (핵심 에러 수정 부분)
    // 부모가 null인 경우와 값이 있는 경우를 동시에 처리하는 쿼리입니다.
    @Query("SELECT COUNT(d) > 0 FROM Directory d " +
           "WHERE d.name = :name " +
           "AND d.isDeleted = false " +
           "AND (" +
           "   (:parentId IS NULL AND d.parent IS NULL) " +      // 부모가 없는 루트 폴더끼리 비교
           "   OR " +
           "   (d.parent.dirId = :parentId) " +                  // ✅ 부모 객체의 dirId를 비교
           ")")
    boolean existsByParentIdAndName(
            @Param("parentId") Long parentId, 
            @Param("name") String name
    );


    // 1. 단순 자식 조회 (부모 권한이 확인된 후 호출됨 -> 필터링 없이 다 가져옴)
    List<Directory> findAllByParent_DirIdAndIsDeletedFalse(Long parentId);
    
    // ✅ 3. 루트(최상위) 폴더 목록 조회
    List<Directory> findByParentIsNullAndIsDeletedFalse();
    
	// 2. 루트 폴더(parentId is null) 조회 시 권한 필터링
    // 조건: 부모없음 AND 삭제안됨 AND (내가 주인이거나 OR 내 부서 권한이 있거나)
    @Query("SELECT DISTINCT d FROM Directory d " +
           "LEFT JOIN DirectoryPermission p ON d.dirId = p.directory.dirId " +
           "WHERE d.parent IS NULL " +
           "AND d.isDeleted = false " +
           "AND (" +
           "   d.ownerId = :userId " +       // 1. 생성자(소유자) 체크
           "   OR p.deptId = :deptId " +     // 2. 부서 권한 체크
           ")")
    List<Directory> findPermittedRootDirectories(@Param("userId") Long userId, 
                                                 @Param("deptId") Long deptId);
    
    boolean existsByDirIdAndOwnerId(Long dirId, Long ownerId);

	Optional<Directory> findByDirIdAndIsDeletedFalse(Long dirId);
}
