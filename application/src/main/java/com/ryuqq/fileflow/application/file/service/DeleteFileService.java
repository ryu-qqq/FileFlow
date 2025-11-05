package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.dto.command.DeleteFileCommand;
import com.ryuqq.fileflow.application.file.dto.query.FileMetadataQuery;
import com.ryuqq.fileflow.application.file.port.in.DeleteFileUseCase;
import com.ryuqq.fileflow.application.file.port.out.FileCommandPort;
import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * DeleteFile Service
 *
 * <p>CQRS Command Side - 파일 삭제 (Soft Delete) 구현</p>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>@Transactional - 쓰기 작업</li>
 *   <li>Domain 메서드 호출: FileAsset.softDelete()</li>
 *   <li>Dirty Checking으로 자동 UPDATE</li>
 * </ul>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>FileQueryPort를 통해 파일 조회</li>
 *   <li>파일 존재 및 테넌트 스코프 검증</li>
 *   <li>삭제 권한 검증 (SELF 또는 TENANT)</li>
 *   <li>Domain 메서드 호출: fileAsset.softDelete()</li>
 *   <li>FileCommandPort를 통해 저장 (Dirty Checking)</li>
 * </ol>
 *
 * <p><strong>삭제 규칙:</strong></p>
 * <ul>
 *   <li>Soft Delete만 수행 (deleted_at 타임스탬프 설정)</li>
 *   <li>물리 삭제는 별도 Batch Job에서 처리</li>
 *   <li>삭제된 파일은 조회 불가 (deleted_at IS NULL 필터)</li>
 *   <li>이미 삭제된 파일 재삭제 시 예외 발생</li>
 * </ul>
 *
 * <p><strong>권한 검증</strong>:</p>
 * <ul>
 *   <li>SELF: 파일 소유자만 삭제 가능 (requesterId == ownerUserId)</li>
 *   <li>TENANT: 테넌트 관리자 삭제 가능 (TODO: RBAC 연동)</li>
 *   <li>ORGANIZATION: 조직 관리자 삭제 가능 (TODO: RBAC 연동)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class DeleteFileService implements DeleteFileUseCase {

    private final FileQueryPort fileQueryPort;
    private final FileCommandPort fileCommandPort;

    /**
     * 생성자
     *
     * @param fileQueryPort 파일 조회 Port
     * @param fileCommandPort 파일 저장 Port
     */
    public DeleteFileService(
        FileQueryPort fileQueryPort,
        FileCommandPort fileCommandPort
    ) {
        this.fileQueryPort = fileQueryPort;
        this.fileCommandPort = fileCommandPort;
    }

    /**
     * 파일 삭제 (Soft Delete)
     *
     * @param command 파일 삭제 Command
     * @throws IllegalArgumentException 파일이 존재하지 않는 경우
     * @throws IllegalStateException 삭제 권한 없음 또는 이미 삭제된 파일
     */
    @Transactional
    @Override
    public void execute(DeleteFileCommand command) {
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

        // 2. 삭제 권한 검증 (SELF: 파일 소유자만 삭제 가능)
        // TODO: RBAC 연동하여 TENANT, ORGANIZATION 권한 검증
        if (!command.requesterId().equals(fileAsset.getOwnerUserId())) {
            throw new IllegalStateException(
                String.format(
                    "Access denied: requesterId=%s does not own fileId=%s (ownerId=%s)",
                    command.requesterId(),
                    fileAsset.getIdValue(),
                    fileAsset.getOwnerUserId()
                )
            );
        }

        // 3. Domain 메서드 호출 - Soft Delete
        // FileAsset.softDelete()는 이미 삭제된 파일에 대해 예외를 던집니다.
        fileAsset.softDelete();

        // 4. 저장 (Dirty Checking으로 자동 UPDATE)
        fileCommandPort.save(fileAsset);
    }
}
