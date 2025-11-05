package com.ryuqq.fileflow.application.download.assembler;

import com.ryuqq.fileflow.application.download.dto.command.StartExternalDownloadCommand;
import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;
import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.upload.UploadSession;

/**
 * ExternalDownloadAssembler - ExternalDownload DTO ↔ Domain 변환 유틸리티
 *
 * <p>Application Layer에서 DTO와 Domain 객체 간의 변환을 담당하는 Assembler 클래스입니다.
 * Hexagonal Architecture의 Port-Adapter 패턴에서 DTO와 Domain의 명확한 분리를 보장합니다.</p>
 *
 * <p><strong>Assembler Pattern 규칙 준수:</strong></p>
 * <ul>
 *   <li>✅ Pure Java (Lombok 금지)</li>
 *   <li>✅ Static 메서드만 제공 (유틸리티 클래스)</li>
 *   <li>✅ Law of Demeter 준수 (Getter 체이닝 금지)</li>
 *   <li>✅ 양방향 변환: Domain → Response</li>
 * </ul>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * ExternalDownload download = ExternalDownload.forNew(sourceUrl, uploadSessionId);
 * UploadSession session = UploadSession.create(tenantId, fileName, fileSize);
 * ExternalDownloadResponse response = ExternalDownloadAssembler.toResponse(download, session);
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public final class ExternalDownloadAssembler {

    /**
     * Private Constructor - 인스턴스 생성 방지
     *
     * <p>유틸리티 클래스이므로 인스턴스를 생성할 수 없습니다.</p>
     *
     * @throws AssertionError 인스턴스 생성 시도 시
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    private ExternalDownloadAssembler() {
        throw new AssertionError("Cannot instantiate utility class");
    }

    /**
     * Command + UploadSession → ExternalDownload Domain 변환
     *
     * <p><strong>용도:</strong></p>
     * <ul>
     *   <li>StartExternalDownloadCommand를 받아서 ExternalDownload 도메인 객체 생성</li>
     *   <li>URL 검증은 ExternalDownload.forNew() 내부에서 수행</li>
     * </ul>
     *
     * @param command StartExternalDownloadCommand
     * @param session UploadSession Aggregate
     * @return ExternalDownload 도메인 객체
     * @throws IllegalArgumentException command 또는 session이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownload toDomain(
        StartExternalDownloadCommand command,
        UploadSession session
    ) {
        if (command == null) {
            throw new IllegalArgumentException("Command는 필수입니다");
        }
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }

        // ExternalDownload.forNew()가 URL 검증을 포함
        return ExternalDownload.forNew(
            command.sourceUrl(),
            session
        );
    }

    /**
     * ExternalDownload + UploadSession → ExternalDownloadResponse 변환 (멱등키 포함)
     *
     * <p><strong>Law of Demeter 준수:</strong></p>
     * <ul>
     *   <li>❌ Bad: download.getId().value(), download.getSourceUrl().toString()</li>
     *   <li>✅ Good: download.getIdValue(), download.getSourceUrlString()</li>
     * </ul>
     *
     * @param download ExternalDownload Aggregate
     * @param session UploadSession Aggregate
     * @param idempotencyKey 멱등키
     * @return ExternalDownloadResponse DTO
     * @throws IllegalArgumentException download, session 또는 idempotencyKey가 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    public static ExternalDownloadResponse toResponse(
        ExternalDownload download,
        UploadSession session,
        String idempotencyKey
    ) {
        if (download == null) {
            throw new IllegalArgumentException("ExternalDownload는 필수입니다");
        }
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }
        if (idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("멱등키는 필수입니다");
        }

        return ExternalDownloadResponse.of(
            idempotencyKey,
            download.getIdValue(),
            session.getIdValue(),
            download.getSourceUrlString(),
            download.getStatus().name()
        );
    }

    /**
     * ExternalDownload + UploadSession → ExternalDownloadResponse 변환 (기존 메서드 - 하위 호환성 유지)
     *
     * @deprecated 멱등키를 포함한 새로운 toResponse 메서드 사용 권장
     * @param download ExternalDownload Aggregate
     * @param session UploadSession Aggregate
     * @return ExternalDownloadResponse DTO
     * @throws IllegalArgumentException download 또는 session이 null인 경우
     * @author Sangwon Ryu
     * @since 1.0.0
     */
    @Deprecated(since = "1.1.0", forRemoval = false)
    public static ExternalDownloadResponse toResponse(
        ExternalDownload download,
        UploadSession session
    ) {
        if (download == null) {
            throw new IllegalArgumentException("ExternalDownload는 필수입니다");
        }
        if (session == null) {
            throw new IllegalArgumentException("UploadSession은 필수입니다");
        }

        // 기존 Response 포맷 유지 (멱등키 없이)
        return ExternalDownloadResponse.ofLegacy(
            download.getIdValue(),
            session.getIdValue(),
            download.getSourceUrlString(),
            download.getStatus().name()
        );
    }
}
