package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.command.DeleteUploadSessionPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveUploadSessionPort;
import com.ryuqq.fileflow.domain.upload.FailureReason;
import com.ryuqq.fileflow.domain.upload.UploadSession;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Upload Session State Manager
 *
 * <p>Upload Session 상태 관리를 전담하는 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Upload Session 저장 (생성 및 업데이트)</li>
 *   <li>Upload Session 삭제</li>
 *   <li>트랜잭션 경계 관리 (Command 전담)</li>
 * </ul>
 *
 * <p><strong>설계 변경:</strong></p>
 * <ul>
 *   <li>✅ CQRS 적용: Command 전담 (Query 메서드 제거)</li>
 *   <li>✅ Port 분리: SaveUploadSessionPort, DeleteUploadSessionPort</li>
 *   <li>✅ StateManager 네이밍 (Manager → StateManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class UploadSessionStateManager {

    private final SaveUploadSessionPort savePort;
    private final DeleteUploadSessionPort deletePort;

    /**
     * 생성자
     *
     * @param savePort Save Upload Session Port (Command)
     * @param deletePort Delete Upload Session Port (Command)
     */
    public UploadSessionStateManager(
        SaveUploadSessionPort savePort,
        DeleteUploadSessionPort deletePort
    ) {
        this.savePort = savePort;
        this.deletePort = deletePort;
    }

    /**
     * Upload Session 저장
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>신규 생성 또는 기존 데이터 업데이트</li>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param session Upload Session Domain Aggregate
     * @return 저장된 Upload Session (ID 포함)
     */
    @Transactional
    public UploadSession save(UploadSession session) {
        return savePort.save(session);
    }

    /**
     * Upload Session 삭제
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param id Upload Session ID
     */
    @Transactional
    public void delete(Long id) {
        deletePort.delete(id);
    }

    /**
     * 만료된 세션을 FAILED 상태로 전환
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>CleanupExpiredSessionsScheduler: 만료된 세션 정리</li>
     *   <li>Session timeout 처리</li>
     * </ul>
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>트랜잭션 내에서 실행</li>
     *   <li>Domain 메서드 호출 후 저장</li>
     * </ul>
     *
     * <p><strong>Domain 설계:</strong></p>
     * <ul>
     *   <li>Tell, Don't Ask: {@code session.fail(reason)} 호출</li>
     *   <li>Domain Aggregate가 상태 전환 로직 소유</li>
     *   <li>StateManager는 저장만 담당</li>
     * </ul>
     *
     * @param session Upload Session Domain Aggregate (Not Null)
     * @param reason 실패 사유 (Not Null, Not Empty)
     * @return 저장된 Upload Session (FAILED 상태)
     * @throws IllegalArgumentException session 또는 reason이 null인 경우
     */
    @Transactional
    public UploadSession failExpiredSession(UploadSession session, String reason) {
        // String → FailureReason VO 변환
        FailureReason failureReason = FailureReason.of(reason);

        // Domain 메서드 호출 (Tell, Don't Ask)
        session.fail(failureReason);

        // 저장
        return savePort.save(session);
    }


}

