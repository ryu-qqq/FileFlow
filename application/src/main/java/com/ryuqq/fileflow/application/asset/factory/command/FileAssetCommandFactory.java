package com.ryuqq.fileflow.application.asset.factory.command;

import com.ryuqq.fileflow.application.asset.dto.processor.UploadedImage;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.aggregate.ProcessedFileAsset;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateResult;
import com.ryuqq.fileflow.domain.asset.service.FileAssetUpdateService;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetId;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.ImageFormat;
import com.ryuqq.fileflow.domain.asset.vo.ImageVariant;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.FileSize;
import java.time.Clock;
import org.springframework.stereotype.Component;

/**
 * FileAsset Command Factory.
 *
 * <p>Command DTO에서 Domain 객체 생성 및 상태 변경을 위한 Clock을 제공합니다.
 *
 * <p><strong>책임:</strong>
 *
 * <ul>
 *   <li>Clock 제공 (Domain 상태 변경 시 필요)
 *   <li>FileAssetUpdateService 인스턴스 생성
 *   <li>FileAssetStatusHistory 생성
 *   <li>ProcessedFileAsset 생성
 * </ul>
 *
 * <p><strong>규칙:</strong>
 *
 * <ul>
 *   <li>@Component 어노테이션 (Service 아님)
 *   <li>비즈니스 로직 금지 (순수 변환)
 *   <li>Port 호출 금지 (조회 없음)
 *   <li>@Transactional 금지
 * </ul>
 */
@Component
public class FileAssetCommandFactory {

    private final ClockHolder clockHolder;
    private final FileAssetUpdateService fileAssetUpdateService;

    public FileAssetCommandFactory(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
        this.fileAssetUpdateService = new FileAssetUpdateService(clockHolder);
    }

    /**
     * 현재 Clock을 반환합니다.
     *
     * @return Clock 인스턴스
     */
    public Clock getClock() {
        return clockHolder.getClock();
    }

    /**
     * FileAssetUpdateService를 반환합니다.
     *
     * <p>Domain Service 인스턴스를 통해 상태 변경 + StatusHistory 생성을 수행합니다.
     *
     * @return FileAssetUpdateService 인스턴스
     */
    public FileAssetUpdateService getUpdateService() {
        return fileAssetUpdateService;
    }

    /**
     * 시스템 변경에 의한 StatusHistory를 생성합니다.
     *
     * @param fileAssetId FileAsset ID
     * @param fromStatus 이전 상태
     * @param toStatus 이후 상태
     * @param reason 변경 사유
     * @return FileAssetStatusHistory
     */
    public FileAssetStatusHistory createStatusHistory(
            FileAssetId fileAssetId,
            FileAssetStatus fromStatus,
            FileAssetStatus toStatus,
            String reason) {
        return FileAssetStatusHistory.forSystemChange(
                fileAssetId, fromStatus, toStatus, reason, null, clockHolder.getClock());
    }

    /**
     * 처리 시작 상태로 변경합니다.
     *
     * @param fileAsset 대상 FileAsset
     * @return 상태 변경 결과
     */
    public FileAssetUpdateResult startProcessing(FileAsset fileAsset) {
        return fileAssetUpdateService.startProcessing(fileAsset);
    }

    /**
     * 리사이징 완료 상태로 변경합니다.
     *
     * @param fileAsset 대상 FileAsset
     * @param processedCount 처리된 이미지 수
     * @return 상태 변경 결과
     */
    public FileAssetUpdateResult markResized(FileAsset fileAsset, int processedCount) {
        return fileAssetUpdateService.markResized(fileAsset, processedCount);
    }

    /**
     * 처리 실패 상태로 변경합니다.
     *
     * @param fileAsset 대상 FileAsset
     * @param errorMessage 에러 메시지
     * @return 상태 변경 결과
     */
    public FileAssetUpdateResult markFailed(FileAsset fileAsset, String errorMessage) {
        return fileAssetUpdateService.markFailed(fileAsset, errorMessage);
    }

    /**
     * Outbox를 발송 완료 상태로 변경합니다.
     *
     * @param outbox 대상 FileProcessingOutbox
     */
    public void markOutboxAsSent(FileProcessingOutbox outbox) {
        outbox.markAsSent(clockHolder.getClock());
    }

    // ==================== Domain 객체 생성 ====================

    /**
     * UploadedImage로부터 ProcessedFileAsset을 생성합니다.
     *
     * <p>UploadedImage에 포함된 ProcessedImageInfo를 활용하여 도메인 정보를 추출합니다. dimension과 colorSpace는 저장하지
     * 않습니다 (스펙에서 결정됨).
     *
     * @param originalAsset 원본 FileAsset
     * @param uploadedImage 업로드된 이미지 결과
     * @return ProcessedFileAsset
     */
    public ProcessedFileAsset createProcessedFileAsset(
            FileAsset originalAsset, UploadedImage uploadedImage) {

        String processedFileName =
                generateProcessedFileName(
                        originalAsset.getFileNameValue(),
                        uploadedImage.variant(),
                        uploadedImage.format());

        return ProcessedFileAsset.forNew(
                originalAsset.getId(),
                uploadedImage.variant(),
                uploadedImage.format(),
                FileName.of(processedFileName),
                FileSize.of(uploadedImage.fileSize()),
                uploadedImage.bucket(),
                uploadedImage.s3Key(),
                originalAsset.getUserId(),
                originalAsset.getOrganizationId(),
                originalAsset.getTenantId(),
                clockHolder.getClock());
    }

    /**
     * 처리된 파일명을 생성합니다.
     *
     * <p>예: product.jpg + THUMBNAIL + WEBP → product_thumb.webp
     *
     * @param originalFileName 원본 파일명
     * @param variant 이미지 변형
     * @param format 이미지 포맷
     * @return 처리된 파일명
     */
    private String generateProcessedFileName(
            String originalFileName, ImageVariant variant, ImageFormat format) {

        int lastDotIndex = originalFileName.lastIndexOf('.');
        String baseName =
                (lastDotIndex > 0) ? originalFileName.substring(0, lastDotIndex) : originalFileName;

        return baseName + variant.suffix() + "." + format.extension();
    }
}
