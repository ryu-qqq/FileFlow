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

