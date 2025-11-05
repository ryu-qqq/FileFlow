package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.UploadSessionPort;
import com.ryuqq.fileflow.domain.upload.FailureReason;
import com.ryuqq.fileflow.domain.upload.SessionKey;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Upload Session Manager
 *
 * <p>Upload Session 상태 관리를 전담하는 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session 저장 (생성 및 업데이트)</li>
 *   <li>Upload Session 조회 (ID, Session Key)</li>
 *   <li>상태 변경 메서드 (start, complete, fail)</li>
 *   <li>트랜잭션 경계 관리</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>InitSingleUploadService: 세션 저장</li>
 *   <li>CompleteUploadService: 세션 완료 처리</li>
 *   <li>FailUploadService: 세션 실패 처리</li>
 * </ul>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>조회 메서드: readOnly=true</li>
 *   <li>상태 변경 메서드: readOnly=false (기본값)</li>
 * </ul>
 *
 * <p><strong>패턴:</strong></p>
 * <ul>
 *   <li>Manager Pattern: 도메인 객체의 생명주기 관리</li>
 *   <li>Transaction Script Pattern: 트랜잭션 경계 명확화</li>
 *   <li>Facade Pattern: UploadSessionPort 캡슐화</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionManager {

    private final UploadSessionPort uploadSessionPort;

    /**
     * 생성자
     *
     * @param uploadSessionPort Upload Session Port
     */
    public UploadSessionManager(UploadSessionPort uploadSessionPort) {
        this.uploadSessionPort = uploadSessionPort;
    }

    /**
     * Upload Session 저장
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>신규 생성 또는 기존 데이터 업데이트</li>
     *   <li>트랜잭션 내에서 실행 (외부 UseCase의 @Transactional 활용)</li>
     * </ul>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    @Transactional
    public UploadSession save(UploadSession session) {
        return uploadSessionPort.save(session);
    }

    /**
     * ID로 Upload Session 조회
     *
     * @param id Upload Session ID
     * @return Upload Session (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<UploadSession> findById(Long id) {
        return uploadSessionPort.findById(id);
    }

    /**
     * Session Key로 Upload Session 조회
     *
     * @param sessionKey Session Key
     * @return Upload Session (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<UploadSession> findBySessionKey(SessionKey sessionKey) {
        return uploadSessionPort.findBySessionKey(sessionKey);
    }

    /**
     * Upload Session 시작
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Upload Session 조회</li>
     *   <li>Domain 메서드 호출: session.start()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> PENDING → IN_PROGRESS</p>
     *
     * @param id Upload Session ID
     * @return 시작된 Upload Session
     * @throws IllegalArgumentException 세션이 존재하지 않는 경우
     * @throws IllegalStateException 이미 시작되었거나 완료된 세션인 경우
     */
    @Transactional
    public UploadSession start(Long id) {
        UploadSession session = uploadSessionPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Upload Session not found: " + id
            ));

        session.start();

        return uploadSessionPort.save(session);
    }

    /**
     * Upload Session 완료
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Upload Session 조회</li>
     *   <li>Domain 메서드 호출: session.complete(fileId)</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> IN_PROGRESS → COMPLETED</p>
     *
     * @param id Upload Session ID
     * @param fileId 생성된 File ID
     * @return 완료된 Upload Session
     * @throws IllegalArgumentException 세션이 존재하지 않는 경우
     * @throws IllegalStateException 진행 중 상태가 아닌 경우
     */
    @Transactional
    public UploadSession complete(Long id, Long fileId) {
        UploadSession session = uploadSessionPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Upload Session not found: " + id
            ));

        session.complete(fileId);

        return uploadSessionPort.save(session);
    }

    /**
     * Upload Session 실패 처리
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Upload Session 조회</li>
     *   <li>Domain 메서드 호출: session.fail(reason)</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → FAILED</p>
     *
     * @param id Upload Session ID
     * @param reason 실패 사유
     * @return 실패 처리된 Upload Session
     * @throws IllegalArgumentException 세션이 존재하지 않는 경우
     * @throws IllegalStateException 이미 완료된 세션인 경우
     */
    @Transactional
    public UploadSession fail(Long id, FailureReason reason) {
        UploadSession session = uploadSessionPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Upload Session not found: " + id
            ));

        session.fail(reason);

        return uploadSessionPort.save(session);
    }

    /**
     * Session Key로 Upload Session 완료
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Session Key로 Upload Session 조회</li>
     *   <li>Domain 메서드 호출: session.complete(fileId)</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * @param sessionKey Session Key
     * @param fileId 생성된 File ID
     * @return 완료된 Upload Session
     * @throws IllegalArgumentException 세션이 존재하지 않는 경우
     * @throws IllegalStateException 진행 중 상태가 아닌 경우
     */
    @Transactional
    public UploadSession completeBySessionKey(SessionKey sessionKey, Long fileId) {
        UploadSession session = uploadSessionPort.findBySessionKey(sessionKey)
            .orElseThrow(() -> new IllegalArgumentException(
                "Upload Session not found with sessionKey: " + sessionKey.value()
            ));

        session.complete(fileId);

        return uploadSessionPort.save(session);
    }

    /**
     * Session Key로 Upload Session 실패 처리
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Session Key로 Upload Session 조회</li>
     *   <li>Domain 메서드 호출: session.fail(reason)</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * @param sessionKey Session Key
     * @param reason 실패 사유
     * @return 실패 처리된 Upload Session
     * @throws IllegalArgumentException 세션이 존재하지 않는 경우
     * @throws IllegalStateException 이미 완료된 세션인 경우
     */
    @Transactional
    public UploadSession failBySessionKey(SessionKey sessionKey, FailureReason reason) {
        UploadSession session = uploadSessionPort.findBySessionKey(sessionKey)
            .orElseThrow(() -> new IllegalArgumentException(
                "Upload Session not found with sessionKey: " + sessionKey.value()
            ));

        session.fail(reason);

        return uploadSessionPort.save(session);
    }
}
