package com.ryuqq.fileflow.domain.asset.service;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.aggregate.FileAssetStatusHistory;
import com.ryuqq.fileflow.domain.asset.aggregate.FileProcessingOutbox;
import com.ryuqq.fileflow.domain.asset.event.FileProcessingRequestedEvent;
import com.ryuqq.fileflow.domain.asset.vo.FileAssetStatus;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;

/**
 * FileAsset 생성 Domain Service.
 *
 * <p>이벤트로부터 FileAsset을 생성하고, 관련 Aggregate들(StatusHistory, Outbox)과 도메인 이벤트를 함께 생성합니다.
 *
 * <p><strong>DDD 원칙</strong>:
 *
 * <ul>
 *   <li>이벤트 → FileAsset 변환은 도메인 로직 (Application Assembler가 아닌 Domain Service에서 처리)
 *   <li>도메인 이벤트는 도메인 라이프사이클에서 생성 (Facade가 아닌 Domain Service에서 생성)
 *   <li>Application Layer(Facade)는 이벤트를 발행만 할 뿐, 생성하지 않음
 * </ul>
 *
 * <p><strong>책임</strong>:
 *
 * <ul>
 *   <li>이벤트 → FileAsset 변환 (파일 카테고리 결정 포함)
 *   <li>FileAsset과 관련된 부가 Aggregate 생성 조율
 *   <li>도메인 불변식 보장 (FileAsset 생성 시 반드시 History, Outbox, DomainEvent 동반)
 * </ul>
 *
 * <p><strong>주의</strong>: 이 서비스는 순수 도메인 로직만 포함하며, 영속성이나 이벤트 발행은 Application Layer에서 처리합니다.
 */
public class FileAssetCreationService {

    private static final String EVENT_TYPE_PROCESS_REQUEST = "PROCESS_REQUEST";
    private static final String INITIAL_CREATION_MESSAGE = "FileAsset 생성됨";

    private final ClockHolder clockHolder;

    public FileAssetCreationService(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * 파일 업로드 완료 이벤트로부터 FileAsset과 관련 Aggregate들을 생성합니다.
     *
     * <p>생성되는 Aggregate 및 이벤트:
     *
     * <ul>
     *   <li>FileAsset: 이벤트로부터 변환
     *   <li>FileAssetStatusHistory: 초기 상태 변경 이력 (null → PENDING)
     *   <li>FileProcessingOutbox: 가공 요청 Outbox (PENDING)
     *   <li>FileProcessingRequestedEvent: 파일 가공 요청 도메인 이벤트
     * </ul>
     *
     * @param event 파일 업로드 완료 이벤트
     * @return FileAssetCreationResult (FileAsset + StatusHistory + Outbox + DomainEvent)
     */
    public FileAssetCreationResult createFromUploadEvent(FileUploadCompletedEvent event) {
        // 1. Event → FileAsset 변환 (카테고리 결정 포함)
        FileAsset fileAsset = toFileAssetFromUploadEvent(event);

        // 2. 관련 Aggregate 및 이벤트 생성
        return createWithRelatedAggregatesAndEvent(fileAsset);
    }

    /**
     * 외부 다운로드 완료 이벤트로부터 FileAsset과 관련 Aggregate들을 생성합니다.
     *
     * <p>생성되는 Aggregate 및 이벤트:
     *
     * <ul>
     *   <li>FileAsset: 이벤트로부터 변환
     *   <li>FileAssetStatusHistory: 초기 상태 변경 이력 (null → PENDING)
     *   <li>FileProcessingOutbox: 가공 요청 Outbox (PENDING)
     *   <li>FileProcessingRequestedEvent: 파일 가공 요청 도메인 이벤트
     * </ul>
     *
     * @param event 외부 다운로드 파일 생성 이벤트
     * @return FileAssetCreationResult (FileAsset + StatusHistory + Outbox + DomainEvent)
     */
    public FileAssetCreationResult createFromExternalDownloadEvent(
            ExternalDownloadFileCreatedEvent event) {
        // 1. Event → FileAsset 변환
        FileAsset fileAsset = toFileAssetFromExternalDownloadEvent(event);

        // 2. 관련 Aggregate 및 이벤트 생성
        return createWithRelatedAggregatesAndEvent(fileAsset);
    }

    /**
     * 파일 업로드 완료 이벤트로부터 FileAsset을 생성합니다.
     *
     * @param event 파일 업로드 완료 이벤트
     * @return FileAsset
     */
    private FileAsset toFileAssetFromUploadEvent(FileUploadCompletedEvent event) {
        FileCategory category = FileCategory.fromMimeType(event.contentType().type());

        return FileAsset.forNew(
                event.sessionId(),
                event.fileName(),
                event.fileSize(),
                event.contentType(),
                category,
                null, // ImageDimension: 업로드 시점에는 알 수 없음, 이미지 처리 시 추출
                event.bucket(),
                event.s3Key(),
                event.etag(),
                event.userId(),
                event.organizationId(),
                event.tenantId(),
                clockHolder.getClock());
    }

    /**
     * 외부 다운로드 파일 생성 이벤트로부터 FileAsset을 생성합니다.
     *
     * @param event 외부 다운로드 파일 생성 이벤트
     * @return FileAsset
     */
    private FileAsset toFileAssetFromExternalDownloadEvent(ExternalDownloadFileCreatedEvent event) {
        return FileAsset.forNew(
                null, // ExternalDownload는 세션 없음
                event.fileName(),
                event.fileSize(),
                event.contentType(),
                event.category(), // ExternalDownload는 이미 카테고리가 결정됨
                null, // ImageDimension: 다운로드 시점에는 알 수 없음, 이미지 처리 시 추출
                event.bucket(),
                event.s3Key(),
                event.etag(),
                null, // ExternalDownload는 userId 없음
                event.organizationId(),
                event.tenantId(),
                clockHolder.getClock());
    }

    /**
     * FileAsset과 관련 Aggregate들, 그리고 도메인 이벤트를 생성합니다.
     *
     * @param fileAsset 생성된 FileAsset
     * @return FileAssetCreationResult (FileAsset + StatusHistory + Outbox + DomainEvent)
     */
    private FileAssetCreationResult createWithRelatedAggregatesAndEvent(FileAsset fileAsset) {
        // 1. 초기 상태 이력 생성
        FileAssetStatusHistory statusHistory = createInitialHistory(fileAsset);

        // 2. 가공 요청 Outbox 생성
        FileProcessingOutbox outbox = createProcessRequestOutbox(fileAsset);

        // 3. 도메인 이벤트 생성 (도메인 라이프사이클에 연결)
        FileProcessingRequestedEvent domainEvent = createDomainEvent(fileAsset, outbox);

        return new FileAssetCreationResult(fileAsset, statusHistory, outbox, domainEvent);
    }

    /**
     * 초기 상태 이력을 생성합니다.
     *
     * @param fileAsset FileAsset
     * @return 초기 상태 이력 (null → PENDING)
     */
    private FileAssetStatusHistory createInitialHistory(FileAsset fileAsset) {
        return FileAssetStatusHistory.forSystemChange(
                fileAsset.getId(),
                null, // fromStatus: 최초 생성이므로 null
                FileAssetStatus.PENDING,
                INITIAL_CREATION_MESSAGE,
                null, // durationMillis: 최초 생성이므로 null
                clockHolder.getClock());
    }

    /**
     * 가공 요청 Outbox를 생성합니다.
     *
     * @param fileAsset FileAsset
     * @return FileProcessingOutbox (PENDING)
     */
    private FileProcessingOutbox createProcessRequestOutbox(FileAsset fileAsset) {
        String payload = createPayload(fileAsset);
        return FileProcessingOutbox.forProcessRequest(
                fileAsset.getId(), EVENT_TYPE_PROCESS_REQUEST, payload, clockHolder.getClock());
    }

    /**
     * 도메인 이벤트를 생성합니다.
     *
     * <p>DDD 원칙에 따라 도메인 라이프사이클에서 이벤트를 생성합니다.
     *
     * @param fileAsset FileAsset
     * @param outbox FileProcessingOutbox
     * @return FileProcessingRequestedEvent
     */
    private FileProcessingRequestedEvent createDomainEvent(
            FileAsset fileAsset, FileProcessingOutbox outbox) {
        return FileProcessingRequestedEvent.of(
                outbox.getId(),
                fileAsset.getId(),
                EVENT_TYPE_PROCESS_REQUEST,
                outbox.getPayload(),
                clockHolder.getClock());
    }

    /**
     * Outbox 페이로드를 생성합니다.
     *
     * @param fileAsset FileAsset
     * @return JSON 페이로드
     */
    private String createPayload(FileAsset fileAsset) {
        return String.format("{\"fileAssetId\":\"%s\"}", fileAsset.getId().getValue());
    }
}
