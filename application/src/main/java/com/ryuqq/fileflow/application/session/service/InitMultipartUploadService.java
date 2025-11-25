package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.session.assembler.MultiPartUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.InitMultipartUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitMultipartUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.in.command.InitMultipartUploadUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindCompletedPartQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.CompletedPart;
import com.ryuqq.fileflow.domain.session.aggregate.MultipartUploadSession;
import com.ryuqq.fileflow.domain.session.vo.S3UploadId;
import com.ryuqq.fileflow.domain.session.vo.S3UploadMetadata;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Multipart 파일 업로드 세션 초기화 Service.
 *
 * <p>대용량 파일 업로드를 위한 Multipart 세션을 생성하고 Part별 Presigned URL을 발급합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>Command → S3UploadMetadata (S3 초기화에 필요한 최소 정보만 생성)
 *   <li>S3 Multipart Upload 초기화 (Upload ID 발급)
 *   <li>Upload ID로 세션 생성 (Domain 불변성 유지)
 *   <li>세션 활성화 및 저장 (Facade: RDB 저장 → Part URLs 생성 → 활성화 → RDB 업데이트 → Redis)
 *   <li>Response 변환 (activatedSession에서 Part URLs 추출)
 * </ol>
 *
 * <p><strong>최적화 포인트</strong>:
 *
 * <ul>
 *   <li>불필요한 temp 세션 생성 제거 (S3UploadMetadata 사용)
 *   <li>세션은 한 번만 생성 (Upload ID 발급 후)
 *   <li>Part URLs는 Domain에 저장 (Facade가 관리)
 * </ul>
 */
@Service
public class InitMultipartUploadService implements InitMultipartUploadUseCase {

    private final MultiPartUploadAssembler multiPartUploadAssembler;
    private final UploadSessionFacade uploadSessionFacade;
    private final FindCompletedPartQueryPort findCompletedPartQueryPort;

    public InitMultipartUploadService(
            MultiPartUploadAssembler multiPartUploadAssembler,
            UploadSessionFacade uploadSessionFacade,
            FindCompletedPartQueryPort findCompletedPartQueryPort) {
        this.multiPartUploadAssembler = multiPartUploadAssembler;
        this.uploadSessionFacade = uploadSessionFacade;
        this.findCompletedPartQueryPort = findCompletedPartQueryPort;
    }

    @Override
    public InitMultipartUploadResponse execute(InitMultipartUploadCommand command) {
        // 1. Command → S3 업로드 메타데이터 (최소 정보만 생성)
        S3UploadMetadata s3Metadata = multiPartUploadAssembler.toS3Metadata(command);

        // 2. S3 Multipart Upload 초기화 (Upload ID 발급 - Facade 위임)
        S3UploadId s3UploadId = uploadSessionFacade.initiateMultipartUpload(s3Metadata);

        // 3. Upload ID로 세션 생성 (Domain 불변성 유지)
        MultipartUploadSession session = multiPartUploadAssembler.toDomain(command, s3UploadId);

        // 4. 세션 활성화 및 저장 (Facade: RDB 저장 → Part URLs 생성 → 활성화 → RDB 업데이트 → Redis)
        MultipartUploadSession activatedSession =
                uploadSessionFacade.createAndActivateMultipartUpload(session);

        // 5. Part 목록 조회
        List<CompletedPart> completedParts =
                findCompletedPartQueryPort.findAllBySessionId(activatedSession.getId());

        // 6. Response 변환
        return multiPartUploadAssembler.toResponse(activatedSession, completedParts);
    }
}
