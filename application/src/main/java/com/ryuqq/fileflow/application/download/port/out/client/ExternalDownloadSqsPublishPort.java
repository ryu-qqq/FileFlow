package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.application.download.dto.ExternalDownloadMessage;

/**
 * SQS 발행 포트.
 *
 * <p>ExternalDownload 처리를 위한 SQS 메시지 발행
 */
public interface ExternalDownloadSqsPublishPort {

    /**
     * SQS에 메시지를 발행합니다.
     *
     * @param message ExternalDownload 처리 메시지
     * @return 발행 성공 여부
     */
    boolean publish(ExternalDownloadMessage message);
}
