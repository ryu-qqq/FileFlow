package com.ryuqq.fileflow.application.download.port.in.command;

/**
 * 외부 다운로드 실패 처리 UseCase.
 *
 * <p>DLQ(Dead Letter Queue)에서 최종 실패한 외부 다운로드를 FAILED 상태로 변경합니다.
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>ExternalDownload 조회
 *   <li>이미 COMPLETED 또는 FAILED 상태인 경우 skip
 *   <li>PENDING 상태인 경우 → PROCESSING → FAILED
 *   <li>PROCESSING 상태인 경우 → FAILED
 *   <li>디폴트 이미지 할당
 * </ul>
 */
public interface MarkExternalDownloadAsFailedUseCase {

    /**
     * ExternalDownload를 최종 실패 상태로 변경합니다.
     *
     * @param externalDownloadId ExternalDownload ID (UUID 문자열)
     * @param errorMessage 에러 메시지
     */
    void markAsFailed(String externalDownloadId, String errorMessage);
}
