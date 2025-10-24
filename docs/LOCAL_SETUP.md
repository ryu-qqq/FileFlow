# FileFlow 로컬 개발 환경 설정 가이드

## 📋 사전 요구사항

- **Java 21** (JDK 21 이상)
- **Docker** (MySQL, Redis 컨테이너용)
- **MySQL 8.0** (로컬 설치 또는 Docker)
- **Gradle 8.x** (프로젝트에 포함된 Wrapper 사용)

---

## 🚀 빠른 시작 (Quick Start)

### 1. Docker Compose로 인프라 시작

```bash
# Redis만 시작 (로컬 MySQL을 사용하는 경우)
docker-compose up -d redis

# MySQL + Redis 모두 시작 (Docker로 MySQL 사용하는 경우)
docker-compose up -d
```

### 2. 데이터베이스 설정

**로컬 MySQL 사용 시:**
```bash
# fileflow 데이터베이스 생성
mysql -u root -p
CREATE DATABASE fileflow CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**Docker MySQL 사용 시:**
- `docker-compose.yml`에서 자동으로 생성됨 (포트: 3307)

### 3. 환경 변수 설정

```bash
# .env 파일 생성 또는 직접 export
export DB_USER=root
export DB_PASSWORD=your_mysql_password
export REDIS_HOST=localhost
export REDIS_PORT=6379
```

### 4. 애플리케이션 빌드 및 실행

```bash
# 전체 빌드 (테스트 제외)
./gradlew clean build -x test

# Spring Boot 실행
./gradlew :bootstrap:bootstrap-web-api:bootRun

# 또는 JAR 파일로 직접 실행
java -jar bootstrap/bootstrap-web-api/build/libs/fileflow-web-api.jar
```

### 5. Swagger UI 접근

애플리케이션이 시작되면 다음 URL로 접근:

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/api-docs

---

## 🔧 상세 설정

### Docker Compose 설정

**파일 위치**: `docker-compose.yml`

```yaml
services:
  mysql:
    image: mysql:8.0
    ports:
      - "3307:3306"  # 로컬 3306 포트 충돌 방지
    environment:
      MYSQL_ROOT_PASSWORD: password
      MYSQL_DATABASE: fileflow
      MYSQL_USER: fileflow_user
      MYSQL_PASSWORD: fileflow_password

  redis:
    image: redis:7.2-alpine
    ports:
      - "6379:6379"
```

**사용 명령어:**
```bash
# 시작
docker-compose up -d

# 로그 확인
docker-compose logs -f

# 중지
docker-compose down

# 중지 + 볼륨 삭제 (데이터 초기화)
docker-compose down -v
```

### Application 설정

**파일 위치**: `bootstrap/bootstrap-web-api/src/main/resources/application.yml`

**주요 설정값:**
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fileflow
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:password}

  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}

springdoc:
  swagger-ui:
    enabled: true
    path: /swagger-ui.html
```

### Flyway 마이그레이션

**위치**: `adapter-out/persistence-mysql/src/main/resources/db/migration/`

Flyway는 애플리케이션 시작 시 자동으로 실행됩니다:
- `V1__Create_tenant_table.sql`
- `V2__Create_organization_table.sql`

**마이그레이션 비활성화 (개발 시):**
```bash
java -jar fileflow-web-api.jar --spring.flyway.enabled=false
```

---

## 🧪 테스트 실행

### 전체 테스트
```bash
./gradlew test
```

### 특정 모듈 테스트
```bash
# Domain layer
./gradlew :domain:test

# Application layer
./gradlew :application:test

# REST API
./gradlew :adapter-in:rest-api:test
```

### ArchUnit 테스트 (아키텍처 검증)
```bash
./gradlew :application:test --tests "*ArchitectureTest"
```

---

## 📊 주요 엔드포인트

### Tenant API
- `GET /api/v1/tenants` - Tenant 목록 조회
- `POST /api/v1/tenants` - Tenant 생성
- `PUT /api/v1/tenants/{tenantId}` - Tenant 수정
- `PATCH /api/v1/tenants/{tenantId}/status` - Tenant 상태 변경

### Organization API
- `GET /api/v1/tenants/{tenantId}/organizations` - Organization 목록 조회
- `POST /api/v1/tenants/{tenantId}/organizations` - Organization 생성
- `PUT /api/v1/organizations/{organizationId}` - Organization 수정
- `PATCH /api/v1/organizations/{organizationId}/status` - Organization 상태 변경

### 헬스 체크
- `GET /actuator/health` - 애플리케이션 상태 확인
- `GET /actuator/info` - 애플리케이션 정보
- `GET /actuator/metrics` - 메트릭 정보

---

## ❗ 트러블슈팅

### MySQL 접속 오류
```
Access denied for user 'root'@'localhost' (using password: YES)
```

**해결 방법:**
1. MySQL root 비밀번호 확인
2. 환경 변수 또는 application.yml에서 비밀번호 수정
3. MySQL 사용자 권한 확인

### Public Key Retrieval 에러
```
Public Key Retrieval is not allowed
```

**해결 방법:**
- `application.yml`에서 URL에 `allowPublicKeyRetrieval=true` 추가 (이미 적용됨)

### 포트 충돌 (3306, 8080)
```
Port already in use
```

**해결 방법:**
1. 다른 MySQL 인스턴스 종료: `sudo lsof -i :3306`
2. Docker MySQL 포트 변경: `3307:3306`
3. Spring Boot 포트 변경: `--server.port=8081`

### Redis 연결 오류
```
Unable to connect to Redis
```

**해결 방법:**
```bash
# Redis 컨테이너 상태 확인
docker-compose ps

# Redis 재시작
docker-compose restart redis
```

---

## 🎯 다음 단계

1. **Swagger UI**에서 API 테스트
2. **Integration Test** 실행으로 전체 흐름 검증
3. **Postman Collection** 생성 (선택 사항)
4. **로컬 개발 데이터** 스크립트 작성

---

## 📚 참고 문서

- [Architecture Tests](../application/src/test/java/com/ryuqq/fileflow/architecture/)
- [Integration Tests](../adapter-in/rest-api/src/test/java/)
- [Coding Conventions](./coding_convention/)
- [Getting Started Tutorial](./tutorials/01-getting-started.md)
