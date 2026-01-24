# 📂 문서 관리 시스템 (DMS, Document Management System) 서버

> **계층형 디렉토리 구조와 부서 기반 접근 제어를 지원하는 문서 관리 시스템 백엔드**

이 프로젝트는 기업/조직 내에서 파일을 안전하게 저장하고 공유하기 위한 RESTful API 서버입니다.  
**Spring Boot**와 **JPA**를 기반으로 하며, **MinIO**를 활용한 파일 저장, **계층형 폴더 구조**, 그리고 **권한 관리(RBAC)** 기능을 제공합니다.  
**Java와 Spring Boot** 생태계에 입문하며, 백엔드의 핵심 개념에 대한 이해 및 학습을 목표로 합니다.

---

## 🛠 Tech Stack

- **Java**: 17
- **Framework**: Spring Boot 4.0.1
- **Persistence**: Spring Data JPA, QueryDSL (Optional)
- **Database**: PostgreSQL (Relations & Meta Data)
- **Object Storage**: MinIO (File Content Storage)
- **Build Tool**: Gradle

---

## 🌟 Key Features

### 1. 계층형 디렉토리 관리 (Hierarchical Directory)
- **무한 깊이 폴더 구조**: 자기 참조(Self-Referencing) 엔티티 설계를 통해 깊이 제한 없는 폴더 구조 지원.
- **Cascade 삭제**: 상위 폴더 삭제 시 하위 폴더 및 포함된 문서, 권한 데이터 일괄 정리 (Data Integrity).

### 2. 접근 제어 (Access Control)
- **소유자(Owner) 권한**: 폴더 생성자는 모든 권한을 가짐.
- **부서 공유(Department Permission)**: 특정 부서에 폴더 접근 권한(Read/Write) 부여 가능.
- **권한 로직**: `Owner OR Shared_Department` 조건의 최적화된 JPQL 쿼리로 단일 트랜잭션 내 권한 검증.

### 3. 트랜잭션 보장 파일 업로드 (Transactional Upload)
- **데이터 정합성**: MinIO(스토리지) 업로드와 DB 메타데이터 저장 간의 트랜잭션 처리.
- **Rollback 전략**: 스토리지 업로드 실패 시 DB 저장 롤백, DB 저장 실패 시 업로드된 파일 정리(보상 트랜잭션) 혹은 예외 처리 순서 제어.
