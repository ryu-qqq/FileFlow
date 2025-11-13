package com.ryuqq.fileflow.application.download.port.out;

import com.ryuqq.fileflow.domain.download.ExternalDownload;

/**
 * External Download Command Port (CQRS - Command Side)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 쓰기 전용 Port 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>External Download Aggregate의 영속화 인터페이스 정의 (쓰기 전용)</li>
 *   <li>Adapter 구현체와 Application Layer 간 계약</li>
 *   <li>도메인 용어 사용 (JPA/DB 용어 금지)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴의 Port 역할</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ 비즈니스 의미 있는 메서드명</li>
 *   <li>✅ Infrastructure 독립적</li>
 *   <li>✅ CQRS - Command 전용 (쓰기만)</li>
 * </ul>
 *
 * <p><strong>CQRS 분리:</strong></p>
 * <ul>
 *   <li>✅ Command: save, delete</li>
 *   <li>❌ Query: findById, findByStatus 등은 QueryPort 사용</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ExternalDownloadCommandPort {

    /**
     * External Download 저장
     *
     * <p>신규 생성 또는 기존 데이터 업데이트를 수행합니다.</p>
     *
     * @param download External Download Domain Aggregate
     * @return 저장된 External Download (ID 포함)
     */
    ExternalDownload save(ExternalDownload download);

    /**
     * External Download 삭제
     *
     * @param id External Download ID
     */
    void delete(Long id);
}

