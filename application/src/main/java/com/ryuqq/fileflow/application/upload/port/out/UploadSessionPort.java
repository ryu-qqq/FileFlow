package com.ryuqq.fileflow.application.upload.port.out;

import com.ryuqq.fileflow.domain.upload.UploadSession;
import com.ryuqq.fileflow.domain.upload.vo.IdempotencyKey;

import java.util.List;
import java.util.Optional;

/**
 * 업로드 세션 저장소 Port
 *
 * Hexagonal Architecture의 Outbound Port로서,
 * 업로드 세션의 영속성 관리를 위한 인터페이스입니다.
 *
 * @author sangwon-ryu
 */
public interface UploadSessionPort {

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
     * 멱등성 키로 업로드 세션을 조회합니다.
     *
     * 중복 요청 방지를 위해 멱등성 키로 기존 세션을 검색합니다.
     *
     * @param idempotencyKey 멱등성 키
     * @return 조회된 업로드 세션 (Optional)
     */
    Optional<UploadSession> findByIdempotencyKey(IdempotencyKey idempotencyKey);

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

    /**
     * 만료된 업로드 세션 목록을 조회합니다.
     *
     * 다음 조건을 만족하는 세션들을 반환합니다:
     * - 상태가 PENDING 또는 UPLOADING
     * - expiresAt이 현재 시간보다 이전 (만료됨)
     *
     * 배치 작업에서 만료된 세션들을 자동으로 FAILED 상태로 전환하기 위해 사용됩니다.
     *
     * @return 만료된 업로드 세션 목록
     */
    List<UploadSession> findExpiredSessions();
}
