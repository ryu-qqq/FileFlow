# 👥 사용자 컨텍스트 & 권한 테이블 명세

## ⚠️ 중요: 인증/인가 서버 분리 아키텍처

**FileFlow는 사용자 인증(Authentication)을 외부 인증/인가 서버에 위임합니다.**

- **인증/인가 서버**: 로그인, 회원가입, 비밀번호 관리, MFA, JWT 발급
- **FileFlow**: 사용자 컨텍스트 관리, FileFlow 전용 권한 관리

```
┌─────────────────────────────────────────────────────────────┐
│                  요청 플로우                                  │
└─────────────────────────────────────────────────────────────┘

1. Client → Auth Server: 로그인
2. Auth Server → Client: JWT 토큰 발급
3. Client → FileFlow API: Authorization: Bearer {jwt}
4. FileFlow API Gateway:
   - JWT 검증 (Auth Server 공개키)
   - Payload에서 user_id, tenant_id, roles 추출
   - 헤더 주입: X-User-Id, X-Tenant-Id, X-Organization-Id
5. FileFlow Service:
   - 헤더에서 사용자 정보 읽기
   - user_contexts에서 FileFlow 전용 정보 조회
   - 권한 검증 후 비즈니스 로직 수행
```

---

## 1. user_contexts (사용자 컨텍스트)

### 테이블 설명
FileFlow 시스템 내에서 사용자 컨텍스트를 관리하는 테이블입니다.
**인증 정보는 외부 서버에서 관리**하며, FileFlow는 최소한의 컨텍스트만 유지합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | FileFlow 내부 ID |
| external_user_id | VARCHAR(100) | UK, NOT NULL | - | 인증서버의 사용자 ID |
| tenant_id | VARCHAR(50) | NOT NULL | - | 소속 테넌트 ID |
| organization_id | BIGINT | NULL | NULL | 소속 조직 ID |
| user_type | ENUM('SELLER', 'COMPANY_ADMIN', 'INTERNAL_ADMIN', 'CUSTOMER', 'SYSTEM') | NOT NULL | - | 사용자 유형 |
| display_name | VARCHAR(100) | NULL | NULL | 표시명 (캐싱용) |
| email | VARCHAR(200) | NULL | NULL | 이메일 (캐싱용) |
| preferences | JSON | NULL | '{}' | FileFlow 사용자 설정 (언어, 타임존 등) |
| last_activity_at | DATETIME | NULL | NULL | 마지막 활동 시각 |
| status | ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') | NOT NULL | 'ACTIVE' | 상태 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| deleted_at | DATETIME | NULL | NULL | 삭제 시각 (soft delete) |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_external_user_id (external_user_id)
INDEX idx_tenant_org (tenant_id, organization_id)
INDEX idx_user_type_status (user_type, status)
INDEX idx_last_activity (last_activity_at)
INDEX idx_deleted_at (deleted_at)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO user_contexts (external_user_id, tenant_id, organization_id, user_type, display_name, email, status) VALUES
('auth_user_001', 'b2c_kr', 1, 'SELLER', '김판매', 'seller@fashionplus.com', 'ACTIVE'),
('auth_user_002', 'b2c_kr', 2, 'INTERNAL_ADMIN', '박관리', 'admin@fileflow.com', 'ACTIVE'),
('auth_user_003', 'b2b_global', 3, 'COMPANY_ADMIN', 'John Smith', 'admin@globaltrade.com', 'ACTIVE'),
('guest_12345', 'b2c_kr', NULL, 'CUSTOMER', '비회원', NULL, 'ACTIVE');
```

### 사용자 컨텍스트 생성 로직

```java
@Service
public class UserContextService {

    /**
     * JWT 토큰에서 추출한 정보로 FileFlow 사용자 컨텍스트 생성/조회
     */
    @Transactional
    public UserContext getOrCreateUserContext(String externalUserId, String tenantId,
                                                Long organizationId, String userType) {
        // 1. 기존 컨텍스트 조회
        return userContextRepository.findByExternalUserId(externalUserId)
            .orElseGet(() -> {
                // 2. 없으면 생성
                UserContext context = UserContext.builder()
                    .externalUserId(externalUserId)
                    .tenantId(tenantId)
                    .organizationId(organizationId)
                    .userType(userType)
                    .status(UserStatus.ACTIVE)
                    .build();

                return userContextRepository.save(context);
            });
    }

    /**
     * 인증서버에서 받은 사용자 정보로 캐시 업데이트
     */
    public void updateUserCache(String externalUserId, String displayName, String email) {
        userContextRepository.findByExternalUserId(externalUserId)
            .ifPresent(context -> {
                context.updateDisplayInfo(displayName, email);
                userContextRepository.save(context);
            });
    }
}
```

---

## 2. roles (역할)

### 테이블 설명
권한 그룹을 정의하는 역할(Role) 테이블입니다. RBAC(Role-Based Access Control) 구현의 핵심입니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 역할 ID |
| tenant_id | VARCHAR(50) | NULL | NULL | 테넌트 ID (NULL=시스템 역할) |
| role_code | VARCHAR(50) | UK, NOT NULL | - | 역할 코드 |
| role_name | VARCHAR(100) | NOT NULL | - | 역할명 |
| description | TEXT | NULL | NULL | 역할 설명 |
| role_type | ENUM('SYSTEM', 'TENANT', 'CUSTOM') | NOT NULL | 'CUSTOM' | 역할 유형 |
| priority | INT | NOT NULL | 100 | 우선순위 (낮을수록 높음) |
| is_assignable | BOOLEAN | NOT NULL | TRUE | 할당 가능 여부 |
| max_users | INT | NULL | NULL | 최대 할당 사용자 수 |
| metadata | JSON | NULL | '{}' | 추가 메타데이터 |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |
| updated_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |
| created_by | VARCHAR(100) | NULL | NULL | 생성자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_role_code (role_code)
UNIQUE KEY uk_tenant_role_code (tenant_id, role_code)
INDEX idx_tenant_id (tenant_id)
INDEX idx_role_type (role_type)
INDEX idx_is_assignable (is_assignable)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO roles (tenant_id, role_code, role_name, description, role_type, priority) VALUES
(NULL, 'SUPER_ADMIN', '슈퍼 관리자', '시스템 전체 관리 권한', 'SYSTEM', 1),
(NULL, 'SYSTEM_ADMIN', '시스템 관리자', '시스템 운영 권한', 'SYSTEM', 10),
('b2c_kr', 'TENANT_ADMIN', '테넌트 관리자', 'B2C 테넌트 관리 권한', 'TENANT', 20),
('b2c_kr', 'SELLER_ADMIN', '판매자 관리자', '판매자 전체 권한', 'CUSTOM', 30),
('b2c_kr', 'SELLER_OPERATOR', '판매자 운영자', '판매자 운영 권한', 'CUSTOM', 40),
('b2b_global', 'COMPANY_ADMIN', '회사 관리자', '입점회사 관리 권한', 'CUSTOM', 30);
```

---

## 3. permissions (권한)

### 테이블 설명
시스템의 세부 권한을 정의하는 테이블입니다. 리소스와 액션의 조합으로 권한을 표현합니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 권한 ID |
| permission_code | VARCHAR(100) | UK, NOT NULL | - | 권한 코드 |
| permission_name | VARCHAR(200) | NOT NULL | - | 권한명 |
| resource_type | VARCHAR(50) | NOT NULL | - | 리소스 타입 (file, upload, user 등) |
| action | VARCHAR(50) | NOT NULL | - | 액션 (create, read, update, delete 등) |
| description | TEXT | NULL | NULL | 권한 설명 |
| scope | ENUM('GLOBAL', 'TENANT', 'ORGANIZATION', 'SELF') | NOT NULL | 'SELF' | 권한 범위 |
| is_system | BOOLEAN | NOT NULL | FALSE | 시스템 권한 여부 |
| depends_on | BIGINT | NULL | NULL | 선행 필요 권한 ID |
| created_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 생성 시각 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_permission_code (permission_code)
UNIQUE KEY uk_resource_action (resource_type, action)
INDEX idx_resource_type (resource_type)
INDEX idx_action (action)
INDEX idx_scope (scope)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO permissions (permission_code, permission_name, resource_type, action, scope, description) VALUES
-- 파일 관련 권한
('FILE_CREATE', '파일 업로드', 'file', 'create', 'ORGANIZATION', '파일 업로드 권한'),
('FILE_READ', '파일 조회', 'file', 'read', 'ORGANIZATION', '파일 조회 권한'),
('FILE_UPDATE', '파일 수정', 'file', 'update', 'ORGANIZATION', '파일 정보 수정 권한'),
('FILE_DELETE', '파일 삭제', 'file', 'delete', 'ORGANIZATION', '파일 삭제 권한'),
('FILE_DOWNLOAD', '파일 다운로드', 'file', 'download', 'ORGANIZATION', '파일 다운로드 권한'),

-- 업로드 세션 권한
('UPLOAD_SESSION_CREATE', '업로드 세션 생성', 'upload_session', 'create', 'SELF', '업로드 세션 생성 권한'),
('UPLOAD_SESSION_MANAGE', '업로드 세션 관리', 'upload_session', 'manage', 'ORGANIZATION', '업로드 세션 관리 권한'),

-- 파이프라인 권한
('PIPELINE_EXECUTE', '파이프라인 실행', 'pipeline', 'execute', 'ORGANIZATION', '파이프라인 실행 권한'),
('PIPELINE_MANAGE', '파이프라인 관리', 'pipeline', 'manage', 'TENANT', '파이프라인 설정 관리 권한'),

-- 사용자 관리 권한
('USER_CREATE', '사용자 생성', 'user', 'create', 'ORGANIZATION', '사용자 생성 권한'),
('USER_READ', '사용자 조회', 'user', 'read', 'ORGANIZATION', '사용자 정보 조회 권한'),
('USER_UPDATE', '사용자 수정', 'user', 'update', 'ORGANIZATION', '사용자 정보 수정 권한'),
('USER_DELETE', '사용자 삭제', 'user', 'delete', 'ORGANIZATION', '사용자 삭제 권한'),

-- 정책 관리 권한
('POLICY_VIEW', '정책 조회', 'policy', 'read', 'ORGANIZATION', '정책 조회 권한'),
('POLICY_MANAGE', '정책 관리', 'policy', 'manage', 'TENANT', '정책 생성/수정/삭제 권한');
```

---

## 4. user_role_mappings (사용자-역할 매핑)

### 테이블 설명
사용자 컨텍스트와 역할을 매핑하는 관계 테이블입니다. 한 사용자는 여러 역할을 가질 수 있습니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 매핑 ID |
| user_context_id | BIGINT | NOT NULL | - | 사용자 컨텍스트 ID |
| role_id | BIGINT | NOT NULL | - | 역할 ID |
| assigned_by | VARCHAR(100) | NULL | NULL | 할당한 사용자 (external_user_id) |
| assigned_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 할당 시각 |
| expires_at | DATETIME | NULL | NULL | 만료 시각 |
| is_active | BOOLEAN | NOT NULL | TRUE | 활성화 상태 |
| reason | TEXT | NULL | NULL | 할당 사유 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_user_role (user_context_id, role_id)
INDEX idx_user_context (user_context_id)
INDEX idx_role_id (role_id)
INDEX idx_expires_at (expires_at)
INDEX idx_is_active (is_active)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
INSERT INTO user_role_mappings (user_context_id, role_id, assigned_by, reason) VALUES
(1, 4, 'auth_user_002', '판매자 관리자 권한 부여'),  -- seller001 -> SELLER_ADMIN
(2, 3, NULL, '시스템 관리자 초기 설정'),  -- admin001 -> TENANT_ADMIN
(3, 6, 'auth_user_002', '입점회사 관리자 등록');  -- company001 -> COMPANY_ADMIN
```

---

## 5. role_permissions (역할-권한 매핑)

### 테이블 설명
역할과 권한을 매핑하는 관계 테이블입니다. 각 역할은 여러 권한을 포함할 수 있습니다.

### 컬럼 명세

| 컬럼명 | 데이터 타입 | 제약조건 | 기본값 | 설명 |
|--------|------------|----------|--------|------|
| id | BIGINT | PK, AUTO_INCREMENT | - | 매핑 ID |
| role_id | BIGINT | NOT NULL | - | 역할 ID |
| permission_id | BIGINT | NOT NULL | - | 권한 ID |
| conditions | JSON | NULL | NULL | 추가 조건 (ABAC용) |
| granted_at | DATETIME | NOT NULL | CURRENT_TIMESTAMP | 부여 시각 |
| granted_by | VARCHAR(100) | NULL | NULL | 부여자 |

### 인덱스
```sql
PRIMARY KEY (id)
UNIQUE KEY uk_role_permission (role_id, permission_id)
INDEX idx_role_id (role_id)
INDEX idx_permission_id (permission_id)
-- 외래키 제거: 참조 무결성은 애플리케이션 레벨에서 검증
```

### 샘플 데이터
```sql
-- SELLER_ADMIN 역할 권한
INSERT INTO role_permissions (role_id, permission_id, conditions) VALUES
(4, 1, NULL),  -- FILE_CREATE
(4, 2, NULL),  -- FILE_READ
(4, 3, NULL),  -- FILE_UPDATE
(4, 4, NULL),  -- FILE_DELETE
(4, 5, NULL),  -- FILE_DOWNLOAD
(4, 6, NULL),  -- UPLOAD_SESSION_CREATE
(4, 7, NULL),  -- UPLOAD_SESSION_MANAGE
(4, 8, NULL);  -- PIPELINE_EXECUTE

-- SELLER_OPERATOR 역할 권한 (제한적)
INSERT INTO role_permissions (role_id, permission_id, conditions) VALUES
(5, 1, NULL),  -- FILE_CREATE
(5, 2, NULL),  -- FILE_READ
(5, 5, NULL),  -- FILE_DOWNLOAD
(5, 6, NULL);  -- UPLOAD_SESSION_CREATE

-- TENANT_ADMIN 역할 권한 (전체)
INSERT INTO role_permissions (role_id, permission_id)
SELECT 3, id FROM permissions;  -- 모든 권한 부여
```

---

## 6. 관계 다이어그램

```
┌─────────────────────────────────────────────────────┐
│              인증/인가 서버 (외부)                    │
│  - 사용자 정보 (이메일, 비밀번호, MFA 등)             │
│  - JWT 토큰 발급                                     │
└────────────────┬────────────────────────────────────┘
                 │ JWT (X-User-Id: auth_user_001)
┌────────────────▼────────────────────────────────────┐
│                  FileFlow 시스템                     │
│                                                      │
│  user_contexts ────< user_role_mappings >──── roles │
│       │                                        │     │
│       │ (external_user_id: auth_user_001)     │     │
│       │                                        │     │
│       └──< FileFlow 전용 정보           role_permissions
│           (preferences, last_activity)          │    │
│                                                 ▼    │
│                                            permissions
└──────────────────────────────────────────────────────┘
```

---

## 7. 인증/인가 통합 가이드

### 7.1 API Gateway 레벨 JWT 검증

```java
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                   HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        String jwt = extractJwtFromRequest(request);

        if (jwt != null && jwtValidator.validate(jwt)) {
            // JWT Payload 추출
            JwtPayload payload = jwtParser.parse(jwt);

            // 헤더에 사용자 정보 주입
            request.setAttribute("X-User-Id", payload.getUserId());
            request.setAttribute("X-Tenant-Id", payload.getTenantId());
            request.setAttribute("X-Organization-Id", payload.getOrganizationId());
            request.setAttribute("X-User-Type", payload.getUserType());
            request.setAttribute("X-Roles", payload.getRoles());

            // FileFlow 사용자 컨텍스트 조회/생성
            UserContext userContext = userContextService.getOrCreateUserContext(
                payload.getUserId(),
                payload.getTenantId(),
                payload.getOrganizationId(),
                payload.getUserType()
            );

            // SecurityContext에 설정
            SecurityContextHolder.getContext().setAuthentication(
                new FileFlowAuthentication(userContext, payload.getRoles())
            );
        }

        filterChain.doFilter(request, response);
    }
}
```

### 7.2 권한 검증

```java
@Service
public class PermissionService {

    /**
     * 사용자가 특정 권한을 가지고 있는지 검증
     */
    public boolean hasPermission(Long userContextId, String permissionCode) {
        // 1. 사용자의 역할 조회
        List<Role> roles = roleRepository.findByUserContextId(userContextId);

        // 2. 역할의 권한 조회
        Set<String> permissions = roles.stream()
            .flatMap(role -> permissionRepository.findByRoleId(role.getId()).stream())
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());

        return permissions.contains(permissionCode);
    }

    /**
     * 권한 검증 with 캐싱
     */
    @Cacheable(value = "user-permissions", key = "#userContextId")
    public Set<String> getUserPermissions(Long userContextId) {
        List<Role> roles = roleRepository.findByUserContextId(userContextId);

        return roles.stream()
            .flatMap(role -> permissionRepository.findByRoleId(role.getId()).stream())
            .map(Permission::getPermissionCode)
            .collect(Collectors.toSet());
    }
}
```

### 7.3 AOP 기반 권한 체크

```java
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RequirePermission {
    String value(); // Permission code
}

@Aspect
@Component
public class PermissionAspect {

    @Autowired
    private PermissionService permissionService;

    @Before("@annotation(requirePermission)")
    public void checkPermission(JoinPoint joinPoint, RequirePermission requirePermission) {
        FileFlowAuthentication auth =
            (FileFlowAuthentication) SecurityContextHolder.getContext().getAuthentication();

        Long userContextId = auth.getUserContext().getId();
        String requiredPermission = requirePermission.value();

        if (!permissionService.hasPermission(userContextId, requiredPermission)) {
            throw new PermissionDeniedException(requiredPermission);
        }
    }
}

// 사용 예시
@RestController
public class FileController {

    @RequirePermission("FILE_UPLOAD")
    @PostMapping("/files")
    public ResponseEntity<FileDto> uploadFile(@RequestBody UploadRequest request) {
        // 파일 업로드 로직
    }
}
```

---

## 8. 보안 고려사항

### 8.1 인증/인가 분리의 장점
- ✅ 단일 인증 시스템 (SSO 구현 용이)
- ✅ 인증 정보 중복 저장 방지
- ✅ 비밀번호 정책 중앙 관리
- ✅ MFA, OAuth2 등 확장 용이
- ✅ 데이터 동기화 복잡성 제거

### 8.2 세션 관리
- JWT 토큰 유효기간: 1시간
- Refresh 토큰: 7일 (인증서버 관리)
- 토큰 갱신: 인증서버 Refresh Endpoint

### 8.3 권한 검증 성능 최적화
- 권한 체크 결과 Redis 캐싱 (TTL: 5분)
- 사용자 역할 정보 JWT Payload에 포함
- 자주 사용되는 권한 조합 사전 계산
- 대량 사용자 조회 시 페이징 필수

---

## 9. 참조 무결성 검증

**외래키를 제거했으므로, 참조 무결성은 애플리케이션에서 보장합니다.**

### 9.1 사용자 컨텍스트 생성 시 검증

```java
@Transactional
public UserContext createUserContext(CreateUserContextCommand command) {
    // 1. 테넌트 존재 여부 검증
    if (!tenantRepository.existsById(command.getTenantId())) {
        throw new TenantNotFoundException(command.getTenantId());
    }

    // 2. 조직 존재 여부 검증
    if (command.getOrganizationId() != null &&
        !organizationRepository.existsById(command.getOrganizationId())) {
        throw new OrganizationNotFoundException(command.getOrganizationId());
    }

    // 3. 사용자 컨텍스트 생성
    return userContextRepository.save(UserContext.create(command));
}
```

### 9.2 역할 할당 시 검증

```java
@Transactional
public void assignRole(Long userContextId, Long roleId) {
    // 1. 사용자 컨텍스트 존재 확인
    UserContext userContext = userContextRepository.findById(userContextId)
        .orElseThrow(() -> new UserContextNotFoundException(userContextId));

    // 2. 역할 존재 확인
    Role role = roleRepository.findById(roleId)
        .orElseThrow(() -> new RoleNotFoundException(roleId));

    // 3. 역할 할당
    userRoleMappingRepository.save(new UserRoleMapping(userContextId, roleId));
}
```

### 9.3 주기적 정합성 검증

```sql
-- 고아 레코드 검증
SELECT 'user_contexts_orphan' as issue_type, COUNT(*) as count
FROM user_contexts uc
LEFT JOIN tenants t ON uc.tenant_id = t.tenant_id
WHERE t.tenant_id IS NULL

UNION ALL

SELECT 'user_role_mappings_orphan', COUNT(*)
FROM user_role_mappings urm
LEFT JOIN user_contexts uc ON urm.user_context_id = uc.id
WHERE uc.id IS NULL

UNION ALL

SELECT 'role_permissions_orphan', COUNT(*)
FROM role_permissions rp
LEFT JOIN roles r ON rp.role_id = r.id
WHERE r.id IS NULL;
```

---

## 10. 성능 최적화

- 권한 체크 결과 Redis 캐싱 (TTL: 5분)
- 사용자 컨텍스트 정보 세션에 포함
- 자주 사용되는 권한 조합 사전 계산
- 대량 사용자 조회 시 페이징 필수
- display_name, email 캐싱으로 인증서버 호출 최소화
