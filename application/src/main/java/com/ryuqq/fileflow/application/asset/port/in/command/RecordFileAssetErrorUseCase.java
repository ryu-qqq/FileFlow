package com.ryuqq.fileflow.application.asset.port.in.command;

/**
 * FileAsset 에러 메시지 기록 UseCase.
 *
 * <p>파일 처리 중 예외가 발생했을 때 실제 에러 메시지를 저장합니다.
 *
 * <p><strong>사용 시점</strong>:
 *
 * <ul>
 *   <li>FileProcessingSqsListener에서 예외 발생 시 (SQS 재시도 전)
 *   <li>ExternalDownloadSqsListener에서 예외 발생 시
 * </ul>
 *
 * <p><strong>주의사항</strong>:
 *
 * <ul>
 *   <li>상태 변경 없이 에러 메시지만 저장
 *   <li>재시도 시 새로운 에러 메시지로 덮어씌워짐
 *   <li>처리 성공 시 clearError() 호출 필요
 * </ul>
 */
public interface RecordFileAssetErrorUseCase {

    /**
     * FileAsset에 에러 메시지를 기록합니다.
     *
     * <p>기존 에러 메시지가 있으면 덮어씌웁니다.
     *
     * @param fileAssetId FileAsset ID (UUID 문자열)
     * @param errorMessage 에러 메시지 (예외의 getMessage() 또는 상세 정보)
     */
    void recordError(String fileAssetId, String errorMessage);
}
