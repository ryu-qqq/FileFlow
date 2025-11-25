package com.ryuqq.fileflow.application.session.port.out.command;

import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.vo.UploadSessionId;
import java.util.List;

/**
 * CompletedPart 영속화 Command Port.
 *
 * <p>멀티파트 업로드에서 완료된 개별 파트를 RDB에 저장합니다.
 *
 * <p><strong>사용 시점</strong>:
 *
 * <ul>
 *   <li>MarkPartUploadedService: 개별 파트 업로드 완료 시
 *   <li>세션과 파트 저장을 분리하여 효율적인 영속화 처리
 * </ul>
 */
public interface PersistCompletedPartPort {

    /**
     * 완료된 파트를 저장합니다.
     *
     * @param sessionId 세션 ID
     * @param completedPart 완료된 파트 정보
     */
    CompletedPart persist(UploadSessionId sessionId, CompletedPart completedPart);

    /**
     * 여러 개의 완료된 파트를 일괄 저장합니다.
     *
     * @param completedParts 완료된 파트 목록
     */
    void persistAll(List<CompletedPart> completedParts);
}
