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

