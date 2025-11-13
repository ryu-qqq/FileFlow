package com.ryuqq.fileflow.application.upload.port.in;

import com.ryuqq.fileflow.application.upload.dto.command.MarkPartUploadedCommand;

/**
 * 파트 업로드 완료 처리 UseCase
 *
 * <p>클라이언트가 파트 업로드 완료 후 호출하여 업로드된 파트 정보를 기록합니다.</p>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>업로드 세션 조회</li>
 *   <li>Multipart 정보 확인</li>
 *   <li>UploadPart Value Object 생성</li>
 *   <li>파트 추가 (Domain 검증)</li>
 *   <li>저장</li>
 * </ol>
 *
 * <p><strong>Transaction 경계 관리:</strong></p>
 * <ul>
 *   <li>✅ 트랜잭션 내에서만 실행 (외부 API 호출 없음)</li>
 *   <li>✅ Domain 검증 활용</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface MarkPartUploadedUseCase {

    /**
     * 파트 업로드 완료 마킹
     *
     * <p>⭐ 전체 로직이 트랜잭션 내에서 실행됩니다.</p>
     *
     * @param command 파트 업로드 완료 Command
     * @throws IllegalArgumentException 세션을 찾을 수 없는 경우
     * @throws IllegalStateException Multipart 업로드가 아닌 경우
     */
    void execute(MarkPartUploadedCommand command);
}
