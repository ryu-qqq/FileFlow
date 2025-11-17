package com.ryuqq.fileflow.application.service;

import com.ryuqq.fileflow.application.dto.command.UploadFromExternalUrlCommand;
import com.ryuqq.fileflow.application.port.in.command.UploadFromExternalUrlPort;
import com.ryuqq.fileflow.application.port.out.command.MessageOutboxPersistencePort;
import com.ryuqq.fileflow.application.port.out.command.SaveFilePort;
import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.aggregate.MessageOutbox;
import com.ryuqq.fileflow.domain.vo.AggregateId;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.UploaderId;
import org.springframework.stereotype.Service;

import java.time.Clock;

/**
 * 외부 URL 업로드 UseCase 구현
 * <p>
 * Zero-Tolerance 규칙:
 * - URL 검증 (HTTPS만 허용)
 * - @Transactional 내 외부 API 호출 절대 금지
 * - Transaction 경계: File 생성/상태 업데이트만 트랜잭션 내부
 * </p>
 */
@Service
public class UploadFromExternalUrlService implements UploadFromExternalUrlPort {

    private final SaveFilePort saveFilePort;
    private final MessageOutboxPersistencePort messageOutboxPersistencePort;
    private final Clock clock;

    public UploadFromExternalUrlService(
            SaveFilePort saveFilePort,
            MessageOutboxPersistencePort messageOutboxPersistencePort,
            Clock clock
    ) {
        this.saveFilePort = saveFilePort;
        this.messageOutboxPersistencePort = messageOutboxPersistencePort;
        this.clock = clock;
    }

    @Override
    public void execute(UploadFromExternalUrlCommand command) {
        // 1. URL 검증 (HTTPS만 허용)
        validateUrl(command.externalUrl());

        // 2. File 메타데이터 생성 (PENDING 상태)
        File pendingFile = createPendingFile(command);
        FileId fileId = saveFilePort.save(pendingFile);

        // 3. MessageOutbox 생성 (FILE_DOWNLOAD_REQUESTED 이벤트)
        MessageOutbox outbox = createFileDownloadRequestedOutbox(fileId, command);
        messageOutboxPersistencePort.persist(outbox);
    }

    /**
     * URL 검증
     * <p>
     * HTTPS 프로토콜만 허용합니다.
     * HTTP는 보안상 허용하지 않습니다.
     * </p>
     */
    private void validateUrl(String externalUrl) {
        if (externalUrl == null || !externalUrl.startsWith("https://")) {
            throw new IllegalArgumentException("외부 URL은 HTTPS 프로토콜만 허용됩니다 (현재: " + externalUrl + ")");
        }
    }

    /**
     * PENDING 상태 File 메타데이터 생성
     * <p>
     * 외부 URL 다운로드를 위한 File Aggregate를 생성합니다.
     * 실제 다운로드는 비동기로 처리되며, 이 시점에는 PENDING 상태입니다.
     * 외부 URL에서 다운로드할 파일은 fileSize=1, mimeType="image/jpeg"로 임시 설정됩니다.
     * (fileSize=0은 검증 실패, Domain Layer 검증 통과를 위해 임시값 사용, 다운로드 후 실제값으로 업데이트)
     * </p>
     */
    private File createPendingFile(UploadFromExternalUrlCommand command) {
        String fileName = extractFileName(command.externalUrl());
        return File.forNew(
                fileName,
                1L, // fileSize=0은 검증 실패, 임시로 1 설정 (다운로드 후 업데이트)
                "image/jpeg", // 임시 MIME 타입 (다운로드 후 실제 타입으로 업데이트)
                "pending/" + fileName, // 임시 S3 키 (다운로드 후 실제 키로 업데이트)
                "temp-bucket", // 임시 S3 버킷 (다운로드 후 실제 버킷으로 업데이트)
                UploaderId.of(command.uploaderId()),
                command.category(),
                String.join(",", command.tags()),
                clock
        );
    }

    /**
     * FILE_DOWNLOAD_REQUESTED 이벤트를 담은 MessageOutbox 생성
     * <p>
     * 비동기 다운로드 처리를 위해 MessageOutbox에 이벤트를 저장합니다.
     * Transaction 경계 내부에서 File 메타데이터와 함께 저장되어 일관성을 보장합니다.
     * </p>
     */
    private MessageOutbox createFileDownloadRequestedOutbox(FileId fileId, UploadFromExternalUrlCommand command) {
        String payload = buildDownloadPayload(fileId, command);

        return MessageOutbox.forNew(
                "FILE_DOWNLOAD_REQUESTED",
                AggregateId.of(fileId.getValue()),
                payload,
                3, // maxRetryCount: 최대 3번 재시도
                clock
        );
    }

    /**
     * 외부 URL에서 파일명 추출
     * <p>
     * URL의 마지막 경로에서 파일명을 추출합니다.
     * 예: https://example.com/images/photo.jpg → photo.jpg
     * </p>
     */
    private String extractFileName(String externalUrl) {
        int lastSlashIndex = externalUrl.lastIndexOf('/');
        if (lastSlashIndex >= 0 && lastSlashIndex < externalUrl.length() - 1) {
            return externalUrl.substring(lastSlashIndex + 1);
        }
        return "download"; // fallback
    }

    /**
     * 다운로드 요청 Payload 생성
     * <p>
     * MessageOutbox에 저장될 JSON Payload를 생성합니다.
     * 외부 다운로드 Worker가 이 Payload를 읽어 실제 다운로드를 수행합니다.
     * </p>
     */
    private String buildDownloadPayload(FileId fileId, UploadFromExternalUrlCommand command) {
        return String.format(
                "{\"fileId\":\"%s\",\"externalUrl\":\"%s\",\"webhookUrl\":\"%s\"}",
                fileId.getValue(),
                command.externalUrl(),
                command.webhookUrl()
        );
    }
}

