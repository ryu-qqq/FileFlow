package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.FileVariantJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.FileVariantEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.FileVariantJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.SaveFileVariantPort;
import com.ryuqq.fileflow.domain.file.variant.FileVariant;
import com.ryuqq.fileflow.domain.file.variant.FileVariantCreatedEvent;
import com.ryuqq.fileflow.domain.file.variant.FileVariantId;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * FileVariant Command Adapter (Persistence Layer)
 *
 * <p><strong>역할</strong>: FileVariant 저장 Command 구현</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 * <p><strong>구현</strong>: {@code SaveFileVariantPort} 인터페이스 구현</p>
 *
 * <h3>헥사고날 아키텍처 패턴</h3>
 * <ul>
 *   <li>✅ Application Layer의 Port 인터페이스 구현</li>
 *   <li>✅ 의존성 방향: Adapter → Application</li>
 *   <li>✅ Domain ↔ Entity 변환 (Mapper 사용)</li>
 *   <li>❌ 비즈니스 로직 없음 (단순 저장만)</li>
 * </ul>
 *
 * <h3>Transaction 관리</h3>
 * <ul>
 *   <li>✅ {@code @Transactional} - REQUIRED (Default)</li>
 *   <li>✅ Application Service에서 Transaction 시작</li>
 *   <li>❌ 외부 API 호출 없음 (DB 저장만)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see SaveFileVariantPort
 */
@Component
public class FileVariantCommandAdapter implements SaveFileVariantPort {

    private final FileVariantJpaRepository fileVariantJpaRepository;
    private final FileVariantEntityMapper fileVariantEntityMapper;
    private final ApplicationEventPublisher eventPublisher;

    public FileVariantCommandAdapter(
        FileVariantJpaRepository fileVariantJpaRepository,
        FileVariantEntityMapper fileVariantEntityMapper,
        ApplicationEventPublisher eventPublisher
    ) {
        this.fileVariantJpaRepository = fileVariantJpaRepository;
        this.fileVariantEntityMapper = fileVariantEntityMapper;
        this.eventPublisher = eventPublisher;
    }

    /**
     * FileVariant 저장
     *
     * <p>신규 Variant 생성 (ID 없음) → JPA가 ID 자동 할당</p>
     *
     * <p><strong>Domain Event 발행:</strong></p>
     * <ul>
     *   <li>FileVariant.create() 시점에는 ID가 null이므로 이벤트에 null 저장</li>
     *   <li>Adapter에서 실제 ID로 이벤트를 재생성하여 발행</li>
     *   <li>트랜잭션 커밋 시점에 이벤트 발행 (Spring 기본 동작)</li>
     * </ul>
     *
     * @param fileVariant 저장할 FileVariant (Domain)
     * @return 저장된 FileVariant (ID 할당됨)
     */
    @Override
    @Transactional
    public FileVariant save(FileVariant fileVariant) {
        // 1. Domain → Entity 변환
        FileVariantJpaEntity entity = fileVariantEntityMapper.toEntity(fileVariant);

        // 2. JPA 저장 (ID 자동 할당)
        FileVariantJpaEntity savedEntity = fileVariantJpaRepository.save(entity);

        // 3. Entity → Domain 변환 (ID 포함)
        FileVariant savedVariant = fileVariantEntityMapper.toDomain(savedEntity);

        // 4. Domain Event 발행 (트랜잭션 커밋 시점에 발행)
        savedVariant.getDomainEvents().forEach(event -> {
            if (event instanceof FileVariantCreatedEvent createdEvent) {
                // fileVariantId를 실제 ID로 업데이트하여 이벤트 재생성
                FileVariantId fileVariantId = savedVariant.getId();
                FileVariantCreatedEvent updatedEvent = new FileVariantCreatedEvent(
                    fileVariantId, // ✅ 실제 ID
                    createdEvent.fileAssetId(),
                    createdEvent.variantType()
                );
                eventPublisher.publishEvent(updatedEvent);
            }
        });

        // 5. Domain Event 초기화
        savedVariant.clearDomainEvents();

        return savedVariant;
    }
}
