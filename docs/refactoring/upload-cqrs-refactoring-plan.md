# Upload 바운디드 컨텍스트 CQRS 리팩토링 계획

## 📋 개요

**목표**: Upload 바운디드 컨텍스트에 CQRS 패턴을 적용하여 Spring Standards 코딩 컨벤션 100% 준수

**현재 상태**:
- ❌ CQRS 패턴 미적용 (Command/Query 혼재)
- ❌ Persistence Adapter가 모든 책임 담당 (SRP 위반)
- ❌ Manager 역할 불명확 (Command + Query 모두 처리)
- ❌ Service가 Port + Manager 직접 의존

**목표 상태**:
- ✅ CQRS 패턴 완벽 적용 (Command/Query 분리)
- ✅ Adapter 책임 분리 (CommandAdapter + QueryAdapter)
- ✅ Manager 역할 명확화 (StateManager, Command 전담)
- ✅ Service 의존성 명확화 (StateManager + LoadPort)

---

## 🎯 Task 1: Command Port 생성 (Application Layer)

**작업 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/command/`

### 1-1. SaveUploadSessionPort.java

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/command/SaveUploadSessionPort.java`

```java
package com.ryuqq.fileflow.application.upload.port.out.command;

import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * Upload Session 저장 Port (Command)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Command Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session Aggregate 저장 (생성 및 업데이트)</li>
 *   <li>CQRS Command 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Port (Write 전담)</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface SaveUploadSessionPort {

    /**
     * Upload Session 저장
     *
     * <p>신규 생성 또는 기존 데이터 업데이트를 수행합니다.</p>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    UploadSession save(UploadSession session);
}
```

### 1-2. DeleteUploadSessionPort.java

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/command/DeleteUploadSessionPort.java`

```java
package com.ryuqq.fileflow.application.upload.port.out.command;

/**
 * Upload Session 삭제 Port (Command)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Command Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session Aggregate 삭제</li>
 *   <li>CQRS Command 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Port (Write 전담)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface DeleteUploadSessionPort {

    /**
     * Upload Session 삭제
     *
     * @param id Upload Session ID
     */
    void delete(Long id);
}
```

### 1-3. SaveMultipartUploadPort.java

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/command/SaveMultipartUploadPort.java`

```java
package com.ryuqq.fileflow.application.upload.port.out.command;

import com.ryuqq.fileflow.domain.upload.MultipartUpload;

/**
 * Multipart Upload 저장 Port (Command)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Command Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload Aggregate 저장 (생성 및 업데이트)</li>
 *   <li>CQRS Command 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Port (Write 전담)</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface SaveMultipartUploadPort {

    /**
     * Multipart Upload 저장
     *
     * <p>신규 생성 또는 기존 데이터 업데이트를 수행합니다.</p>
     *
     * @param multipart Multipart Upload Domain Aggregate
     * @return 저장된 Multipart Upload (ID 포함)
     */
    MultipartUpload save(MultipartUpload multipart);
}
```

### 1-4. DeleteMultipartUploadPort.java

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/command/DeleteMultipartUploadPort.java`

```java
package com.ryuqq.fileflow.application.upload.port.out.command;

/**
 * Multipart Upload 삭제 Port (Command)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Command Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload Aggregate 삭제</li>
 *   <li>CQRS Command 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Port (Write 전담)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface DeleteMultipartUploadPort {

    /**
     * Multipart Upload 삭제
     *
     * @param id Multipart Upload ID
     */
    void delete(Long id);
}
```

---

## 🎯 Task 2: Query Port 생성 (Application Layer)

**작업 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/query/`

### 2-1. LoadUploadSessionPort.java

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/query/LoadUploadSessionPort.java`

```java
package com.ryuqq.fileflow.application.upload.port.out.query;

import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Upload Session 조회 Port (Query)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Query Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session Aggregate 조회</li>
 *   <li>CQRS Query 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Port (Read 전담)</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface LoadUploadSessionPort {

    /**
     * ID로 Upload Session 조회
     *
     * @param id Upload Session ID
     * @return Upload Session (Optional)
     */
    Optional<UploadSession> findById(Long id);

    /**
     * Session Key로 Upload Session 조회
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    Optional<UploadSession> findBySessionKey(SessionKey sessionKey);

    /**
     * 상태와 생성 시간 기준으로 Upload Session 목록 조회
     *
     * @param status 세션 상태
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session 목록
     */
    List<UploadSession> findByStatusAndCreatedBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    );
}
```

### 2-2. LoadMultipartUploadPort.java

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/query/LoadMultipartUploadPort.java`

```java
package com.ryuqq.fileflow.application.upload.port.out.query;

import com.ryuqq.fileflow.domain.upload.MultipartUpload;

import java.util.List;
import java.util.Optional;

/**
 * Multipart Upload 조회 Port (Query)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Query Port입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload Aggregate 조회</li>
 *   <li>CQRS Query 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Port (Read 전담)</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface LoadMultipartUploadPort {

    /**
     * ID로 Multipart Upload 조회
     *
     * @param id Multipart Upload ID
     * @return Multipart Upload (Optional)
     */
    Optional<MultipartUpload> findById(Long id);

    /**
     * Upload Session ID로 Multipart Upload 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return Multipart Upload (Optional)
     */
    Optional<MultipartUpload> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 Multipart Upload 목록 조회
     *
     * @param status Multipart 상태
     * @return Multipart Upload 목록
     */
    List<MultipartUpload> findByStatus(MultipartUpload.MultipartStatus status);
}
```

---

## 🎯 Task 3: Command Adapter 생성 (Persistence Layer)

**작업 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/command/`

### 3-1. UploadSessionCommandAdapter.java

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/command/UploadSessionCommandAdapter.java`

```java
package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.command;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadSessionJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.command.DeleteUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;

/**
 * Upload Session Command Adapter
 *
 * <p>Application Layer의 Command Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate의 영속화 (Write 전담)</li>
 *   <li>CQRS Command Adapter 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Adapter (Write 전담)</li>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionCommandAdapter implements SaveUploadSessionPort, DeleteUploadSessionPort {

    private final UploadSessionJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository Upload Session JPA Repository
     */
    public UploadSessionCommandAdapter(UploadSessionJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * Upload Session 저장
     *
     * <p><strong>저장 처리:</strong></p>
     * <ol>
     *   <li>Domain → Entity 변환</li>
     *   <li>JPA save() 호출</li>
     *   <li>저장된 Entity → Domain 변환</li>
     * </ol>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    @Override
    public UploadSession save(UploadSession session) {
        UploadSessionJpaEntity entity = UploadSessionEntityMapper.toEntity(session);
        UploadSessionJpaEntity saved = repository.save(entity);
        return UploadSessionEntityMapper.toDomain(saved);
    }

    /**
     * Upload Session 삭제
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param id Upload Session ID
     */
    @Override
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
```

### 3-2. MultipartUploadCommandAdapter.java

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/command/MultipartUploadCommandAdapter.java`

```java
package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.command;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.MultipartUploadJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.MultipartUploadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.MultipartUploadJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadPartJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.command.DeleteMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Multipart Upload Command Adapter
 *
 * <p>Application Layer의 Command Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>MultipartUpload Domain Aggregate의 영속화 (Write 전담)</li>
 *   <li>CQRS Command Adapter 패턴 구현</li>
 *   <li>UploadPart 연관 데이터 함께 관리</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Command Adapter (Write 전담)</li>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadCommandAdapter implements SaveMultipartUploadPort, DeleteMultipartUploadPort {

    private final MultipartUploadJpaRepository multipartRepository;
    private final UploadPartJpaRepository partRepository;

    /**
     * 생성자
     *
     * @param multipartRepository Multipart Upload JPA Repository
     * @param partRepository Upload Part JPA Repository
     */
    public MultipartUploadCommandAdapter(
        MultipartUploadJpaRepository multipartRepository,
        UploadPartJpaRepository partRepository
    ) {
        this.multipartRepository = multipartRepository;
        this.partRepository = partRepository;
    }

    /**
     * Multipart Upload 저장
     *
     * <p><strong>저장 처리:</strong></p>
     * <ol>
     *   <li>Multipart Upload Entity 저장</li>
     *   <li>연관된 Upload Part 목록 저장 (기존 삭제 후 재저장)</li>
     *   <li>저장된 데이터로 Domain Aggregate 재구성</li>
     * </ol>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param multipart Multipart Upload Domain Aggregate
     * @return 저장된 Multipart Upload (ID 포함)
     */
    @Override
    public MultipartUpload save(MultipartUpload multipart) {
        // 1. Domain → Entity 변환
        MultipartUploadJpaEntity entity = MultipartUploadEntityMapper.toEntity(multipart);

        // 2. Multipart Upload 저장
        MultipartUploadJpaEntity saved = multipartRepository.save(entity);

        // 3. Upload Parts 저장 (있는 경우)
        if (multipart.getUploadedParts() != null && !multipart.getUploadedParts().isEmpty()) {
            saveUploadParts(saved.getId(), multipart.getUploadedParts());
        }

        // 4. 저장된 데이터로 Domain 재구성
        List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(saved.getId());
        return MultipartUploadEntityMapper.toDomain(saved, parts);
    }

    /**
     * Multipart Upload 삭제
     *
     * <p>연관된 Upload Part도 함께 삭제됩니다 (Cascade).</p>
     *
     * <p><strong>주의</strong>: 트랜잭션은 Application Layer에서 관리됨</p>
     *
     * @param id Multipart Upload ID
     */
    @Override
    public void delete(Long id) {
        // 1. Upload Parts 먼저 삭제
        partRepository.deleteByMultipartUploadId(id);

        // 2. Multipart Upload 삭제
        multipartRepository.deleteById(id);
    }

    /**
     * Upload Parts 저장 (Private Helper)
     *
     * <p>기존 Parts를 삭제하고 새로운 Parts를 저장합니다 (교체 전략).</p>
     *
     * @param multipartUploadId Multipart Upload ID
     * @param parts Upload Part 목록
     */
    private void saveUploadParts(Long multipartUploadId, List<com.ryuqq.fileflow.domain.upload.UploadPart> parts) {
        // 기존 Parts 삭제
        partRepository.deleteByMultipartUploadId(multipartUploadId);

        // 새로운 Parts 저장
        List<UploadPartJpaEntity> entities = MultipartUploadEntityMapper.partsToEntities(parts, multipartUploadId);
        partRepository.saveAll(entities);
    }
}
```

---

## 🎯 Task 4: Query Adapter 생성 (Persistence Layer)

**작업 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/query/`

### 4-1. UploadSessionQueryAdapter.java

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/query/UploadSessionQueryAdapter.java`

```java
package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.UploadSessionEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadSessionJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Upload Session Query Adapter
 *
 * <p>Application Layer의 Query Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>UploadSession Domain Aggregate 조회 (Read 전담)</li>
 *   <li>CQRS Query Adapter 패턴 구현</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Adapter (Read 전담)</li>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionQueryAdapter implements LoadUploadSessionPort {

    private final UploadSessionJpaRepository repository;

    /**
     * 생성자
     *
     * @param repository Upload Session JPA Repository
     */
    public UploadSessionQueryAdapter(UploadSessionJpaRepository repository) {
        this.repository = repository;
    }

    /**
     * ID로 Upload Session 조회
     *
     * @param id Upload Session ID
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findById(Long id) {
        return repository.findById(id)
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * Session Key로 Upload Session 조회
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    @Override
    public Optional<UploadSession> findBySessionKey(SessionKey sessionKey) {
        return repository.findBySessionKey(sessionKey.value())
            .map(UploadSessionEntityMapper::toDomain);
    }

    /**
     * 상태와 생성 시간 기준으로 Upload Session 목록 조회
     *
     * <p>만료된 세션 정리 등에 사용됩니다.</p>
     *
     * @param status 세션 상태
     * @param createdBefore 이 시간 이전에 생성된 세션
     * @return Upload Session 목록
     */
    @Override
    public List<UploadSession> findByStatusAndCreatedBefore(
        SessionStatus status,
        LocalDateTime createdBefore
    ) {
        return repository.findByStatusAndCreatedAtBefore(status, createdBefore)
            .stream()
            .map(UploadSessionEntityMapper::toDomain)
            .collect(Collectors.toList());
    }
}
```

### 4-2. MultipartUploadQueryAdapter.java

**파일 경로**: `adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/query/MultipartUploadQueryAdapter.java`

```java
package com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.adapter.query;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.entity.UploadPartJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.mapper.MultipartUploadEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.MultipartUploadJpaRepository;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.upload.repository.UploadPartJpaRepository;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Multipart Upload Query Adapter
 *
 * <p>Application Layer의 Query Port를 구현하는 Persistence Adapter입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>MultipartUpload Domain Aggregate 조회 (Read 전담)</li>
 *   <li>CQRS Query Adapter 패턴 구현</li>
 *   <li>UploadPart 연관 데이터 함께 조회</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ CQRS Query Adapter (Read 전담)</li>
 *   <li>❌ Persistence Adapter에서 @Transactional 사용 금지</li>
 *   <li>✅ Application Layer (UseCase)에서 트랜잭션 관리</li>
 *   <li>✅ Mapper를 통한 명시적 변환</li>
 *   <li>❌ 비즈니스 로직 포함 금지</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadQueryAdapter implements LoadMultipartUploadPort {

    private final MultipartUploadJpaRepository multipartRepository;
    private final UploadPartJpaRepository partRepository;

    /**
     * 생성자
     *
     * @param multipartRepository Multipart Upload JPA Repository
     * @param partRepository Upload Part JPA Repository
     */
    public MultipartUploadQueryAdapter(
        MultipartUploadJpaRepository multipartRepository,
        UploadPartJpaRepository partRepository
    ) {
        this.multipartRepository = multipartRepository;
        this.partRepository = partRepository;
    }

    /**
     * ID로 Multipart Upload 조회
     *
     * @param id Multipart Upload ID
     * @return Multipart Upload (Optional)
     */
    @Override
    public Optional<MultipartUpload> findById(Long id) {
        return multipartRepository.findById(id)
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(entity.getId());
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            });
    }

    /**
     * Upload Session ID로 Multipart Upload 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return Multipart Upload (Optional)
     */
    @Override
    public Optional<MultipartUpload> findByUploadSessionId(Long uploadSessionId) {
        return multipartRepository.findByUploadSessionId(uploadSessionId)
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(entity.getId());
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            });
    }

    /**
     * 상태별 Multipart Upload 목록 조회
     *
     * @param status Multipart 상태
     * @return Multipart Upload 목록
     */
    @Override
    public List<MultipartUpload> findByStatus(MultipartUpload.MultipartStatus status) {
        return multipartRepository.findByStatus(status)
            .stream()
            .map(entity -> {
                List<UploadPartJpaEntity> parts = partRepository.findByMultipartUploadId(entity.getId());
                return MultipartUploadEntityMapper.toDomain(entity, parts);
            })
            .collect(Collectors.toList());
    }
}
```

---

## 🎯 Task 5: StateManager 리팩토링 (Application Layer)

**작업 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/manager/`

### 5-1. UploadSessionStateManager.java (리팩토링)

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/manager/UploadSessionStateManager.java`

**변경 사항**:
1. 기존 `UploadSessionManager.java`를 `UploadSessionStateManager.java`로 리네임
2. Port 의존성 변경: `UploadSessionPort` → `SaveUploadSessionPort`, `DeleteUploadSessionPort`
3. Query 메서드 제거 (findById, findBySessionKey 등)

```java
package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.command.DeleteUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upload Session State Manager
 *
 * <p>Upload Session 상태 관리를 전담하는 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session 저장 (생성 및 업데이트)</li>
 *   <li>Upload Session 삭제</li>
 *   <li>트랜잭션 경계 관리 (Command 전담)</li>
 * </ul>
 *
 * <p><strong>설계 변경:</strong></p>
 * <ul>
 *   <li>✅ CQRS 적용: Command 전담 (Query 메서드 제거)</li>
 *   <li>✅ Port 분리: SaveUploadSessionPort, DeleteUploadSessionPort</li>
 *   <li>✅ StateManager 네이밍 (Manager → StateManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionStateManager {

    private final SaveUploadSessionPort savePort;
    private final DeleteUploadSessionPort deletePort;

    /**
     * 생성자
     *
     * @param savePort Save Upload Session Port (Command)
     * @param deletePort Delete Upload Session Port (Command)
     */
    public UploadSessionStateManager(
        SaveUploadSessionPort savePort,
        DeleteUploadSessionPort deletePort
    ) {
        this.savePort = savePort;
        this.deletePort = deletePort;
    }

    /**
     * Upload Session 저장
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>신규 생성 또는 기존 데이터 업데이트</li>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    @Transactional
    public UploadSession save(UploadSession session) {
        return savePort.save(session);
    }

    /**
     * Upload Session 삭제
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param id Upload Session ID
     */
    @Transactional
    public void delete(Long id) {
        deletePort.delete(id);
    }
}
```

### 5-2. MultipartUploadStateManager.java (리팩토링)

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/manager/MultipartUploadStateManager.java`

**변경 사항**:
1. 기존 `MultipartUploadManager.java`를 `MultipartUploadStateManager.java`로 리네임
2. Port 의존성 변경: `MultipartUploadPort` → `SaveMultipartUploadPort`, `DeleteMultipartUploadPort`
3. Query 메서드 제거 (findById, findByUploadSessionId 등)

```java
package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.command.DeleteMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Multipart Upload State Manager
 *
 * <p>Multipart Upload 상태 관리를 전담하는 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload 저장 (생성 및 업데이트)</li>
 *   <li>Multipart Upload 삭제</li>
 *   <li>상태 변경 메서드 (complete, abort, fail, addPart)</li>
 *   <li>트랜잭션 경계 관리 (Command 전담)</li>
 * </ul>
 *
 * <p><strong>설계 변경:</strong></p>
 * <ul>
 *   <li>✅ CQRS 적용: Command 전담 (Query 메서드 제거)</li>
 *   <li>✅ Port 분리: SaveMultipartUploadPort, DeleteMultipartUploadPort</li>
 *   <li>✅ StateManager 네이밍 (Manager → StateManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadStateManager {

    private final SaveMultipartUploadPort savePort;
    private final DeleteMultipartUploadPort deletePort;

    /**
     * 생성자
     *
     * @param savePort Save Multipart Upload Port (Command)
     * @param deletePort Delete Multipart Upload Port (Command)
     */
    public MultipartUploadStateManager(
        SaveMultipartUploadPort savePort,
        DeleteMultipartUploadPort deletePort
    ) {
        this.savePort = savePort;
        this.deletePort = deletePort;
    }

    /**
     * Multipart Upload 저장
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>신규 생성 또는 기존 데이터 업데이트</li>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param multipartUpload Multipart Upload Domain Aggregate
     * @return 저장된 Multipart Upload (ID 포함)
     */
    @Transactional
    public MultipartUpload save(MultipartUpload multipartUpload) {
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 완료 (Domain Aggregate 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.complete()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> IN_PROGRESS → COMPLETED</p>
     *
     * @param multipartUpload 완료할 Multipart Upload Domain Aggregate
     * @return 완료된 Multipart Upload
     * @throws IllegalStateException 완료 조건을 만족하지 않는 경우
     */
    @Transactional
    public MultipartUpload complete(MultipartUpload multipartUpload) {
        multipartUpload.complete();
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 중단 (Domain Aggregate 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.abort()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → ABORTED</p>
     *
     * @param multipartUpload 중단할 Multipart Upload Domain Aggregate
     * @return 중단된 Multipart Upload
     * @throws IllegalStateException 이미 완료된 경우
     */
    @Transactional
    public MultipartUpload abort(MultipartUpload multipartUpload) {
        multipartUpload.abort();
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 실패 처리 (Domain Aggregate 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.fail()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → FAILED</p>
     *
     * @param multipartUpload 실패 처리할 Multipart Upload Domain Aggregate
     * @return 실패 처리된 Multipart Upload
     */
    @Transactional
    public MultipartUpload fail(MultipartUpload multipartUpload) {
        multipartUpload.fail();
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload에 파트 추가
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.addPart(part)</li>
     *   <li>파트 추가 사항 저장</li>
     * </ol>
     *
     * @param multipartUpload 파트를 추가할 Multipart Upload Domain Aggregate
     * @param part 업로드된 파트 정보 (UploadPart Value Object)
     * @return 파트가 추가된 Multipart Upload
     * @throws IllegalArgumentException 파트 번호 중복 또는 범위 초과
     */
    @Transactional
    public MultipartUpload addPart(MultipartUpload multipartUpload, UploadPart part) {
        multipartUpload.addPart(part);
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 삭제
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param id Multipart Upload ID
     */
    @Transactional
    public void delete(Long id) {
        deletePort.delete(id);
    }
}
```

---

## 🎯 Task 6: Service 리팩토링 (Application Layer)

**작업 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/service/`

### 6-1. CompleteMultipartUploadService.java (리팩토링)

**파일 경로**: `application/src/main/java/com/ryuqq/fileflow/application/upload/service/CompleteMultipartUploadService.java`

**변경 사항**:
1. Port 의존성 변경: `UploadSessionPort` → `LoadUploadSessionPort`
2. Manager 의존성 변경: `MultipartUploadManager` → `MultipartUploadStateManager`, `LoadMultipartUploadPort`
3. Query는 LoadPort 직접 호출, Command는 StateManager 호출

**리팩토링 후 코드 (주요 변경 부분)**:

```java
@Service
public class CompleteMultipartUploadService implements CompleteMultipartUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(CompleteMultipartUploadService.class);

    // ✅ 변경: Port 분리
    private final LoadUploadSessionPort loadUploadSessionPort;           // Query Port
    private final MultipartUploadStateManager multipartUploadStateManager; // Command Manager
    private final LoadMultipartUploadPort loadMultipartUploadPort;       // Query Port

    private final IamContextFacade iamContextFacade;
    private final S3MultipartFacade s3MultipartFacade;
    private final S3StoragePort s3StoragePort;
    private final FileCommandManager fileCommandManager;
    private final String s3Bucket;

    public CompleteMultipartUploadService(
        LoadUploadSessionPort loadUploadSessionPort,
        MultipartUploadStateManager multipartUploadStateManager,
        LoadMultipartUploadPort loadMultipartUploadPort,
        IamContextFacade iamContextFacade,
        S3MultipartFacade s3MultipartFacade,
        S3StoragePort s3StoragePort,
        FileCommandManager fileCommandManager,
        @Value("${aws.s3.bucket}") String s3Bucket
    ) {
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.multipartUploadStateManager = multipartUploadStateManager;
        this.loadMultipartUploadPort = loadMultipartUploadPort;
        this.iamContextFacade = iamContextFacade;
        this.s3MultipartFacade = s3MultipartFacade;
        this.s3StoragePort = s3StoragePort;
        this.fileCommandManager = fileCommandManager;
        this.s3Bucket = s3Bucket;
    }

    @Transactional(readOnly = true)
    @Override
    public CompleteMultipartResponse execute(CompleteMultipartCommand command) {
        // 1. 완료 가능 검증 (트랜잭션 내)
        ValidationResultResponse validationResultResponse = validateCanComplete(command.sessionKey());
        UploadSession session = validationResultResponse.session();
        MultipartUpload multipart = validationResultResponse.multipart();

        // ... (나머지 로직 동일)
    }

    /**
     * 완료 가능 검증
     *
     * <p>✅ 변경: Query Port 직접 사용</p>
     */
    public ValidationResultResponse validateCanComplete(String sessionKey) {
        // ✅ Query Port 직접 호출
        UploadSession session = loadUploadSessionPort
            .findBySessionKey(SessionKey.of(sessionKey))
            .orElseThrow(() ->
                new IllegalArgumentException("Upload session not found: " + sessionKey)
            );

        // ✅ Query Port 직접 호출
        MultipartUpload multipart = loadMultipartUploadPort
            .findByUploadSessionId(session.getId())
            .orElseThrow(() ->
                new IllegalStateException("Not a multipart upload")
            );

        if (!multipart.canComplete()) {
            throw new IllegalStateException(
                "Cannot complete multipart upload. " +
                "Uploaded parts: " + multipart.getUploadedParts().size() +
                ", Total parts: " + multipart.getTotalParts().value()
            );
        }

        return new ValidationResultResponse(session, multipart);
    }

    /**
     * Domain 상태 업데이트
     *
     * <p>✅ 변경: StateManager 사용</p>
     */
    public void completeUpload(
        UploadSession session,
        MultipartUpload multipart,
        S3CompleteResultResponse s3Result,
        S3HeadObjectResponse s3HeadResult
    ) {
        // 1. MultipartUpload 완료 (StateManager 사용)
        multipartUploadStateManager.complete(multipart);

        // ... (나머지 로직 동일)
    }
}
```

### 6-2. 기타 Service 파일도 동일한 패턴으로 리팩토링

**대상 파일**:
- `InitMultipartUploadService.java`
- `MarkPartUploadedService.java`
- `InitSingleUploadService.java`
- `CompleteSingleUploadService.java`
- `ExpireUploadSessionService.java`
- `GeneratePartPresignedUrlService.java`

**공통 변경 패턴**:
1. `UploadSessionPort` → `LoadUploadSessionPort` (Query) + `UploadSessionStateManager` (Command)
2. `MultipartUploadPort` → `LoadMultipartUploadPort` (Query) + `MultipartUploadStateManager` (Command)
3. Query는 LoadPort 직접 호출
4. Command는 StateManager 호출

---

## 🎯 Task 7: 기존 파일 삭제 (Deprecated)

**삭제 대상**:

### 7-1. Application Layer
```bash
# 기존 Port 인터페이스 삭제
rm application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/UploadSessionPort.java
rm application/src/main/java/com/ryuqq/fileflow/application/upload/port/out/MultipartUploadPort.java
```

### 7-2. Persistence Layer
```bash
# 기존 Adapter 삭제
rm adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/UploadSessionPersistenceAdapter.java
rm adapter-out/persistence-mysql/src/main/java/com/ryuqq/fileflow/adapter/out/persistence/mysql/upload/adapter/MultipartUploadPersistenceAdapter.java
```

---

## 🎯 Task 8: 테스트 업데이트

**작업 범위**: 기존 테스트 파일 수정

### 8-1. Service 테스트

**대상**:
- `CompleteMultipartUploadServiceTest.java`
- `InitMultipartUploadServiceTest.java`
- 기타 Service 테스트

**변경 사항**:
1. Mock 객체 변경: `UploadSessionPort` → `LoadUploadSessionPort`, `UploadSessionStateManager`
2. Mock 객체 변경: `MultipartUploadPort` → `LoadMultipartUploadPort`, `MultipartUploadStateManager`

### 8-2. Adapter 테스트

**대상**:
- `UploadSessionPersistenceAdapterTest.java` → 삭제 후 2개로 분리
  - `UploadSessionCommandAdapterTest.java` (신규)
  - `UploadSessionQueryAdapterTest.java` (신규)
- `MultipartUploadPersistenceAdapterTest.java` → 삭제 후 2개로 분리
  - `MultipartUploadCommandAdapterTest.java` (신규)
  - `MultipartUploadQueryAdapterTest.java` (신규)

---

## 📊 예상 효과

### Before (현재)
- ❌ CQRS 패턴 미적용 (Command/Query 혼재)
- ❌ Adapter가 모든 책임 담당 (SRP 위반)
- ❌ Manager 역할 불명확 (Command + Query)
- ❌ Service가 Port + Manager 직접 의존

### After (리팩토링 후)
- ✅ CQRS 패턴 완벽 적용 (Command/Query 분리)
- ✅ Adapter 책임 분리 (CommandAdapter + QueryAdapter)
- ✅ Manager 역할 명확화 (StateManager, Command 전담)
- ✅ Service 의존성 명확화 (StateManager + LoadPort)
- ✅ Spring Standards 코딩 컨벤션 100% 준수

---

## 🚀 작업 순서 (Cursor에게 순서대로 작업 지시)

1. ✅ **Task 1**: Command Port 4개 생성 (Application Layer)
2. ✅ **Task 2**: Query Port 2개 생성 (Application Layer)
3. ✅ **Task 3**: Command Adapter 2개 생성 (Persistence Layer)
4. ✅ **Task 4**: Query Adapter 2개 생성 (Persistence Layer)
5. ✅ **Task 5**: StateManager 2개 리팩토링 (Application Layer)
6. ✅ **Task 6**: Service 7개 리팩토링 (Application Layer)
7. ✅ **Task 7**: 기존 파일 4개 삭제
8. ✅ **Task 8**: 테스트 업데이트

**총 예상 소요 시간**: 3-4주 (1명 풀타임 기준)

---

## 📝 Checklist

- [ ] Task 1: Command Port 4개 생성 완료
- [ ] Task 2: Query Port 2개 생성 완료
- [ ] Task 3: Command Adapter 2개 생성 완료
- [ ] Task 4: Query Adapter 2개 생성 완료
- [ ] Task 5: StateManager 2개 리팩토링 완료
- [ ] Task 6: Service 7개 리팩토링 완료
- [ ] Task 7: 기존 파일 4개 삭제 완료
- [ ] Task 8: 테스트 업데이트 완료
- [ ] 전체 테스트 실행 (성공 확인)
- [ ] 코드 리뷰 완료
- [ ] PR 생성 및 Merge

---

**작성자**: Claude (Anthropic AI)
**작성일**: 2025-11-05
**버전**: 1.0.0
