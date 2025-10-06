package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.model.UploadSession;

import java.util.Optional;

/**
 * 업로드 세션 저장소 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * 업로드 세션의 영속성 관리를 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface UploadSessionRepository {

    /**
     * 업로드 세션을 저장합니다.
     *
     * @param session 저장할 업로드 세션
     * @return 저장된 업로드 세션
     */
    UploadSession save(UploadSession session);

    /**
     * 세션 ID로 업로드 세션을 조회합니다.
     *
     * @param sessionId 세션 ID
     * @return 조회된 업로드 세션 (Optional)
     */
    Optional<UploadSession> findById(String sessionId);

    /**
     * 업로드 세션이 존재하는지 확인합니다.
     *
     * @param sessionId 세션 ID
     * @return 존재 여부
     */
    boolean existsById(String sessionId);

    /**
     * 업로드 세션을 삭제합니다.
     *
     * @param sessionId 세션 ID
     */
    void deleteById(String sessionId);
}
