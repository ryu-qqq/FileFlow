package com.ryuqq.fileflow.application.upload.service;

import com.ryuqq.fileflow.application.upload.manager.UploadSessionStateManager;
import com.ryuqq.fileflow.application.upload.port.out.query.LoadUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Upload Session 만료 처리 Service
 *
 * <p>Presigned URL이 만료된 UploadSession을 EXPIRED 상태로 변경합니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Redis TTL 만료 이벤트 처리</li>
 *   <li>UploadSession 상태를 EXPIRED로 변경</li>
 *   <li>만료 사유 로깅</li>
 * </ul>
 *
 * <p><strong>호출 경로:</strong></p>
 * <pre>
 * Redis TTL 만료
 *   → RedisKeyExpiredEvent 발행
 *   → UploadSessionExpirationListener 수신
 *   → ExpireUploadSessionService.expire() 호출
 *   → UploadSession.expire() (Domain)
 *   → UploadSessionManager.save() (영속화)
 * </pre>
 *
 * <p><strong>Transaction 경계:</strong></p>
 * <ul>
 *   <li>✅ UploadSessionManager.findBySessionKey(): readOnly=true</li>
 *   <li>✅ UploadSession.expire(): Domain 메서드 (트랜잭션 불필요)</li>
 *   <li>✅ UploadSessionManager.save(): readOnly=false (트랜잭션 내)</li>
 * </ul>
 *
 * <p><strong>예외 처리:</strong></p>
 * <ul>
 *   <li>세션이 없는 경우: 경고 로그 (이미 삭제되었거나 완료된 경우)</li>
 *   <li>이미 만료된 경우: 무시 (멱등성 보장)</li>
 *   <li>DB 오류: 예외 전파 (재시도 가능)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class ExpireUploadSessionService {

    private static final Logger log = LoggerFactory.getLogger(ExpireUploadSessionService.class);

    private final LoadUploadSessionPort loadUploadSessionPort;
    private final UploadSessionStateManager uploadSessionStateManager;

    /**
     * 생성자
     *
     * @param loadUploadSessionPort Load Upload Session Port (Query)
     * @param uploadSessionStateManager Upload Session State Manager (Command)
     */
    public ExpireUploadSessionService(
        LoadUploadSessionPort loadUploadSessionPort,
        UploadSessionStateManager uploadSessionStateManager
    ) {
        this.loadUploadSessionPort = loadUploadSessionPort;
        this.uploadSessionStateManager = uploadSessionStateManager;
    }

    /**
     * Upload Session 만료 처리
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Session Key로 UploadSession 조회</li>
     *   <li>없는 경우: 경고 로그 후 종료 (이미 삭제됨)</li>
     *   <li>있는 경우: UploadSession.expire() 호출</li>
     *   <li>변경 사항 저장 (UploadSessionManager.save())</li>
     *   <li>만료 로그 기록</li>
     * </ol>
     *
     * <p><strong>멱등성 보장:</strong></p>
     * <ul>
     *   <li>UploadSession.expire()는 이미 EXPIRED 상태면 무시</li>
     *   <li>여러 번 호출해도 안전</li>
     * </ul>
     *
     * <p><strong>예외 처리:</strong></p>
     * <ul>
     *   <li>세션 없음: 경고 로그, 정상 종료</li>
     *   <li>DB 오류: 예외 전파 (재시도 가능)</li>
     * </ul>
     *
     * @param sessionKey UploadSession의 세션 키 (SessionKey.value())
     * @throws IllegalArgumentException sessionKey가 null이거나 비어있는 경우
     */
    public void expire(String sessionKey) {
        if (sessionKey == null || sessionKey.isBlank()) {
            throw new IllegalArgumentException("Session key는 필수입니다");
        }

        // 1. UploadSession 조회 (Query Port)
        SessionKey key = SessionKey.of(sessionKey);
        Optional<UploadSession> sessionOpt = loadUploadSessionPort.findBySessionKey(key);

        if (sessionOpt.isEmpty()) {
            // 세션이 없는 경우: 이미 삭제되었거나 완료됨
            log.warn(
                "Upload session not found for expiration: sessionKey={}. " +
                "Session may have been already deleted or completed.",
                sessionKey
            );
            return;
        }

        UploadSession session = sessionOpt.get();

        // 2. Domain 메서드 호출: expire()
        session.expire();

        // 3. 변경 사항 저장 (트랜잭션 내, StateManager 사용)
        uploadSessionStateManager.save(session);

        // 4. 만료 로그
        log.info(
            "Upload session expired due to presigned URL TTL: " +
            "sessionKey={}, uploadType={}, fileName={}",
            sessionKey,
            session.getUploadType(),
            session.getFileName().value()
        );
    }
}
