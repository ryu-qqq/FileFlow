package com.ryuqq.fileflow.application.asset.port.in.command;

/**
 * FileAsset 실패 처리 UseCase.
 *
 * <p>DLQ(Dead Letter Queue)에서 최종 실패한 FileAsset을 FAILED 상태로 변경합니다.
 *
 * <p><strong>처리 내용</strong>:
 *
 * <ul>
 *   <li>FileAsset 조회
 *   <li>이미 COMPLETED 또는 FAILED 상태인 경우 skip
 *   <li>PENDING 상태인 경우 → PROCESSING → FAILED
 *   <li>PROCESSING 상태인 경우 → FAILED
 * </ul>
 *
 * <p><strong>호출 시점</strong>:
 *
 * <ul>
 *   <li>FileProcessingDlqListener에서 DLQ 메시지 수신 시 호출
 *   <li>이미지 처리 파이프라인에서 3회 재시도 후 최종 실패 시
 * </ul>
 */
public interface MarkFileAssetAsFailedUseCase {

    /**
     * FileAsset을 최종 실패 상태로 변경합니다.
     *
     * @param fileAssetId FileAsset ID (UUID 문자열)
     * @param errorMessage 에러 메시지
     */
    void markAsFailed(String fileAssetId, String errorMessage);
}
