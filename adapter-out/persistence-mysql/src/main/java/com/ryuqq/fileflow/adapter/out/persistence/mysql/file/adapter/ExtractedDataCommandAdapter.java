package com.ryuqq.fileflow.adapter.out.persistence.mysql.file.adapter;

import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.entity.ExtractedDataJpaEntity;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.mapper.ExtractedDataEntityMapper;
import com.ryuqq.fileflow.adapter.out.persistence.mysql.file.repository.ExtractedDataJpaRepository;
import com.ryuqq.fileflow.application.file.port.out.SaveExtractedDataPort;
import com.ryuqq.fileflow.domain.file.extraction.ExtractedData;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * ExtractedData Command Adapter (Persistence Layer)
 *
 * <p><strong>역할</strong>: ExtractedData 저장 Command 구현</p>
 * <p><strong>위치</strong>: adapter-out/persistence-mysql/file/adapter/</p>
 * <p><strong>구현</strong>: {@code SaveExtractedDataPort} 인터페이스 구현</p>
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
 * @see SaveExtractedDataPort
 */
@Component
public class ExtractedDataCommandAdapter implements SaveExtractedDataPort {

    private final ExtractedDataJpaRepository extractedDataJpaRepository;
    private final ExtractedDataEntityMapper extractedDataEntityMapper;

    public ExtractedDataCommandAdapter(
        ExtractedDataJpaRepository extractedDataJpaRepository,
        ExtractedDataEntityMapper extractedDataEntityMapper
    ) {
        this.extractedDataJpaRepository = extractedDataJpaRepository;
        this.extractedDataEntityMapper = extractedDataEntityMapper;
    }

    /**
     * ExtractedData 저장
     *
     * <p>신규 생성 (ID 없음) → JPA가 ID 자동 할당</p>
     *
     * @param extractedData 저장할 ExtractedData (Domain)
     * @return 저장된 ExtractedData (ID 할당됨)
     */
    @Override
    @Transactional
    public ExtractedData save(ExtractedData extractedData) {
        // 1. Domain → Entity 변환
        ExtractedDataJpaEntity entity = extractedDataEntityMapper.toEntity(extractedData);

        // 2. JPA 저장 (ID 자동 할당)
        ExtractedDataJpaEntity savedEntity = extractedDataJpaRepository.save(entity);

        // 3. Entity → Domain 변환 (ID 포함)
        ExtractedData savedData = extractedDataEntityMapper.toDomain(savedEntity);

        // 4. Domain Event 처리 (필요 시)
        // ⭐ ExtractedData는 도메인 이벤트를 가지고 있음
        // Application Layer에서 이벤트 발행 처리

        return savedData;
    }
}
