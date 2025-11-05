package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.dto.command.GenerateDownloadUrlCommand;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.dto.response.DownloadUrlResponse;
import com.ryuqq.fileflow.application.file.port.in.GenerateDownloadUrlUseCase;
import com.ryuqq.fileflow.application.file.port.out.DownloadUrlGeneratorPort;
import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * GenerateDownloadUrl Service
 *
 * <p>CQRS Command Side - 파일 다운로드 URL 생성 구현</p>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>@Transactional(readOnly = true) - 읽기 전용 최적화</li>
 *   <li>외부 S3 API 호출 (트랜잭션 밖에서 수행)</li>
 *   <li>Dirty Checking 비활성화 (상태 변경 없음)</li>
 * </ul>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>FileQueryPort를 통해 파일 조회</li>
 *   <li>파일 존재 및 테넌트 스코프 검증</li>
 *   <li>파일 상태 검증 (AVAILABLE, 삭제 여부, 만료 여부)</li>
 *   <li>DownloadUrlGeneratorPort를 통해 S3 Presigned URL 생성</li>
 *   <li>DownloadUrlResponse 생성 및 반환</li>
 * </ol>
 *
 * <p><strong>비즈니스 규칙:</strong></p>
 * <ul>
 *   <li>파일 상태가 AVAILABLE이어야 다운로드 가능</li>
 *   <li>삭제된 파일(deleted_at != null)은 다운로드 불가</li>
 *   <li>만료된 파일은 다운로드 불가</li>
 *   <li>PRIVATE 파일은 소유자 또는 권한 있는 사용자만 다운로드 (TODO: RBAC 연동)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class GenerateDownloadUrlService implements GenerateDownloadUrlUseCase {

    private final FileQueryPort fileQueryPort;
    private final DownloadUrlGeneratorPort downloadUrlGeneratorPort;

    /**
     * 생성자
     *
     * @param fileQueryPort 파일 조회 Port
     * @param downloadUrlGeneratorPort 다운로드 URL 생성 Port
     */
    public GenerateDownloadUrlService(
        FileQueryPort fileQueryPort,
        DownloadUrlGeneratorPort downloadUrlGeneratorPort
    ) {
        this.fileQueryPort = fileQueryPort;
        this.downloadUrlGeneratorPort = downloadUrlGeneratorPort;
    }

    /**
     * 파일 다운로드 URL 생성
     *
     * @param command 다운로드 URL 생성 Command
     * @return DownloadUrlResponse (서명된 URL, 만료 시간)
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     * @throws IllegalStateException 파일 상태가 AVAILABLE이 아닌 경우
     */
    @Transactional(readOnly = true)
    @Override
    public DownloadUrlResponse execute(GenerateDownloadUrlCommand command) {
        // 1. 파일 조회
        FileMetadataQuery query = FileMetadataQuery.of(
            command.fileId(),
            command.tenantId(),
            command.organizationId()
        );

        FileAsset fileAsset = fileQueryPort.findByQuery(query)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format(
                    "File not found: fileId=%s, tenantId=%s",
                    command.fileId().value(),
                    command.tenantId().value()
                )
            ));

        // 2. 다운로드 가능 여부 검증 (Domain 메서드 활용)
        if (!fileAsset.canDownload()) {
            throw new IllegalStateException(
                String.format(
                    "File cannot be downloaded: fileId=%s, status=%s, deleted=%s, expired=%s",
                    fileAsset.getIdValue(),
                    fileAsset.getStatus(),
                    fileAsset.isDeleted(),
                    fileAsset.isExpired()
                )
            );
        }

        // 3. S3 Presigned URL 생성 (외부 API 호출, 트랜잭션 밖)
        String storageKey = fileAsset.getStorageKey().value();
        String downloadUrl = downloadUrlGeneratorPort.generateDownloadUrl(
            storageKey,
            command.expirationDuration()
        );

        // 4. 만료 시간 계산
        LocalDateTime expiresAt = LocalDateTime.now().plus(command.expirationDuration());

        // 5. Response 생성
        return DownloadUrlResponse.of(
            fileAsset.getIdValue(),
            fileAsset.getFileName().value(),
            downloadUrl,
            expiresAt
        );
    }
}
