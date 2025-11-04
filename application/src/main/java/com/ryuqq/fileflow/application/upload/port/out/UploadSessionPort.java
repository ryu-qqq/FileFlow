package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.SessionStatus;
import com.ryuqq.fileflow.domain.upload.UploadSession;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Upload Session Port (Out)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Port 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session Aggregate의 영속화 인터페이스 정의</li>
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
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface UploadSessionPort {

    /**
     * Upload Session 저장
     *
     * <p>신규 생성 또는 기존 데이터 업데이트를 수행합니다.</p>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    UploadSession save(UploadSession session);

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

    /**
     * Upload Session 삭제
     *
     * @param id Upload Session ID
     */
    void delete(Long id);
}
