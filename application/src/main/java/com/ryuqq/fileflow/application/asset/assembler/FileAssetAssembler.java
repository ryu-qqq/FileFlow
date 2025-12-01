package com.ryuqq.fileflow.application.asset.assembler;

import com.ryuqq.fileflow.domain.asset.aggregate.FileAsset;
import com.ryuqq.fileflow.domain.asset.vo.FileCategory;
import com.ryuqq.fileflow.domain.common.util.ClockHolder;
import com.ryuqq.fileflow.domain.download.event.ExternalDownloadFileCreatedEvent;
import com.ryuqq.fileflow.domain.session.event.FileUploadCompletedEvent;
import org.springframework.stereotype.Component;

/** FileUploadCompletedEvent와 ExternalDownloadFileCreatedEvent를 FileAsset으로 변환하는 Assembler. */
@Component
public class FileAssetAssembler {

    private final ClockHolder clockHolder;

    public FileAssetAssembler(ClockHolder clockHolder) {
        this.clockHolder = clockHolder;
    }

    /**
     * 파일 업로드 완료 이벤트로부터 FileAsset을 생성합니다.
     *
     * @param event 파일 업로드 완료 이벤트
     * @return FileAsset
     */
    public FileAsset toFileAsset(FileUploadCompletedEvent event) {
        FileCategory category = determineCategory(event.contentType().type());

        return FileAsset.forNew(
                event.sessionId(),
                event.fileName(),
                event.fileSize(),
                event.contentType(),
                category,
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
    public FileAsset toFileAsset(ExternalDownloadFileCreatedEvent event) {
        return FileAsset.forNew(
                null, // ExternalDownload는 세션 없음
                event.fileName(),
                event.fileSize(),
                event.contentType(),
                event.category(),
                event.bucket(),
                event.s3Key(),
                event.etag(),
                null, // ExternalDownload는 userId 없음
                event.organizationId(),
                event.tenantId(),
                clockHolder.getClock());
    }

    /**
     * ContentType으로부터 FileCategory를 결정합니다.
     *
     * @param contentType 컨텐츠 타입 문자열
     * @return FileCategory
     */
    private FileCategory determineCategory(String contentType) {
        if (contentType == null) {
            return FileCategory.OTHER;
        }

        String lowerType = contentType.toLowerCase();

        if (lowerType.startsWith("image/")) {
            return FileCategory.IMAGE;
        }
        if (lowerType.startsWith("video/")) {
            return FileCategory.VIDEO;
        }
        if (lowerType.startsWith("audio/")) {
            return FileCategory.AUDIO;
        }
        if (lowerType.startsWith("application/pdf")
                || lowerType.startsWith("application/msword")
                || lowerType.startsWith(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml")
                || lowerType.startsWith("application/vnd.ms-excel")
                || lowerType.startsWith(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml")
                || lowerType.startsWith("text/")) {
            return FileCategory.DOCUMENT;
        }

        return FileCategory.OTHER;
    }
}
