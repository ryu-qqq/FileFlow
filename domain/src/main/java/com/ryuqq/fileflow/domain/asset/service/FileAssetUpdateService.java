package com.ryuqq.fileflow.domain.asset.service;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;

/**
 * FileAsset 상태 변경 Domain Service.
 *
 * <p>FileAsset의 상태 변경과 관련 StatusHistory 생성을 담당합니다.
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>상태 변경 로직은 도메인 로직 (Application Layer가 아닌 Domain Service에서 처리)
 *   <li>StatusHistory 생성은 상태 변경의 도메인 불변식 (반드시 함께 생성)
 *   <li>Application Layer(Facade/Manager)는 영속화만 담당
 * </ul>
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>FileAsset 상태 변경 메서드 호출
 *   <li>상태 변경에 따른 StatusHistory 생성
 *   <li>상태 변경 결과 반환 (FileAsset + StatusHistory)
 * </ul>
 *
 * <p><strong>주의</strong>: 이 서비스는 순수 도메인 로직만 포함하며, 영속성이나 이벤트 발행은 Application Layer에서 처리합니다.
 */
public class FileAssetUpdateService {

    private static final String PROCESSING_START_MESSAGE = "이미지 처리 시작";
    private static final String RESIZED_MESSAGE_FORMAT = "이미지 처리 완료: %d개 생성";
    private static final String FAILED_MESSAGE_FORMAT = "처리 실패: %s";

    private final ClockHolder clockHolder;

    public FileAssetUpdateService(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * 처리 시작 상태로 변경합니다.
     *
     * <p>PENDING → PROCESSING 상태로 변경하고 StatusHistory를 생성합니다.
     *
     * @param fileAsset 상태 변경할 FileAsset
     * @return 상태 변경 결과 (FileAsset + StatusHistory)
     */
    public FileAssetUpdateResult startProcessing(FileAsset fileAsset) {
        FileAssetStatus fromStatus = fileAsset.getStatus();

        fileAsset.startProcessing();

        FileAssetStatusHistory statusHistory =
                FileAssetStatusHistory.forSystemChange(
                        fileAsset.getId(),
                        fromStatus,
                        FileAssetStatus.PROCESSING,
                        PROCESSING_START_MESSAGE,
                        null,
                        clockHolder.getClock());

        return new FileAssetUpdateResult(fileAsset, statusHistory);
    }

    /**
     * 리사이징 완료 상태로 변경합니다.
     *
     * <p>PROCESSING → RESIZED 상태로 변경하고 StatusHistory를 생성합니다.
     *
     * @param fileAsset 상태 변경할 FileAsset
     * @param processedCount 처리된 이미지 수
     * @return 상태 변경 결과 (FileAsset + StatusHistory)
     */
    public FileAssetUpdateResult markResized(FileAsset fileAsset, int processedCount) {
        FileAssetStatus fromStatus = fileAsset.getStatus();

        fileAsset.markResized();

        String message = String.format(RESIZED_MESSAGE_FORMAT, processedCount);
        FileAssetStatusHistory statusHistory =
                FileAssetStatusHistory.forSystemChange(
                        fileAsset.getId(),
                        fromStatus,
                        FileAssetStatus.RESIZED,
                        message,
                        null,
                        clockHolder.getClock());

        return new FileAssetUpdateResult(fileAsset, statusHistory);
    }

    /**
     * 처리 실패 상태로 변경합니다.
     *
     * <p>현재 상태 → FAILED 상태로 변경하고 StatusHistory를 생성합니다.
     *
     * @param fileAsset 상태 변경할 FileAsset
     * @param errorMessage 실패 사유
     * @return 상태 변경 결과 (FileAsset + StatusHistory)
     */
    public FileAssetUpdateResult markFailed(FileAsset fileAsset, String errorMessage) {
        FileAssetStatus fromStatus = fileAsset.getStatus();

        fileAsset.failProcessing(clockHolder.getClock());
        fileAsset.recordError(errorMessage);

        String message = String.format(FAILED_MESSAGE_FORMAT, errorMessage);
        FileAssetStatusHistory statusHistory =
                FileAssetStatusHistory.forSystemChange(
                        fileAsset.getId(),
                        fromStatus,
                        FileAssetStatus.FAILED,
                        message,
                        null,
                        clockHolder.getClock());

        return new FileAssetUpdateResult(fileAsset, statusHistory);
    }
}
