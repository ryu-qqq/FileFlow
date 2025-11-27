package com.ryuqq.fileflow.application.download.dto;

import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownload;
import com.ryuqq.fileflow.domain.download.aggregate.ExternalDownloadOutbox;
import java.util.Objects;

/**
 * 외부 다운로드 번들.
 *
 * <p>ExternalDownload와 ExternalDownloadOutbox를 함께 전달하기 위한 내부 DTO입니다.
 *
 * <p><strong>사용 시점</strong>:
 *
 * <ul>
 *   <li>Assembler에서 Command → Bundle 변환
 *   <li>Facade에서 Bundle 저장 및 이벤트 발행
 * </ul>
 *
 * <p><strong>불변 객체</strong>: 내부 객체는 변경 가능하지만 참조 자체는 변경 불가
 */
public record ExternalDownloadBundle(ExternalDownload download, ExternalDownloadOutbox outbox) {

    public ExternalDownloadBundle {
        Objects.requireNonNull(download, "download must not be null");
        Objects.requireNonNull(outbox, "outbox must not be null");
    }
}
