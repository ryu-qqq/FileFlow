package com.ryuqq.fileflow.application.session.service;

import com.ryuqq.fileflow.application.session.assembler.SingleUploadAssembler;
import com.ryuqq.fileflow.application.session.dto.command.InitSingleUploadCommand;
import com.ryuqq.fileflow.application.session.dto.response.InitSingleUploadResponse;
import com.ryuqq.fileflow.application.session.facade.UploadSessionFacade;
import com.ryuqq.fileflow.application.session.port.in.command.InitSingleUploadUseCase;
import com.ryuqq.fileflow.application.session.port.out.query.FindUploadSessionQueryPort;
import com.ryuqq.fileflow.domain.session.aggregate.SingleUploadSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

/**
 * 단일 파일 업로드 세션 초기화 Service.
 *
 * <p>단일 파일 업로드를 위한 세션을 생성하고 Presigned URL을 발급합니다.
 *
 * <p><strong>처리 플로우</strong>:
 *
 * <ol>
 *   <li>Command → Domain (Assembler)
 *   <li>멱등성 키로 기존 세션 조회 (RDB QueryPort)
 *   <li>기존 세션이 없으면 신규 생성 (Facade)
 *   <li>Response 변환 (Assembler)
 *       <ul>
 *         <li>toResponse() 내부에서 getPresignedUrl() 호출
 *         <li>만료된 세션이면 SessionExpiredException 자동 발생 (Tell, Don't Ask)
 *       </ul>
 * </ol>
 *
 * <p><strong>멱등성 보장</strong>:
 *
 * <ul>
 *   <li>동일한 IdempotencyKey로 중복 요청 시 기존 세션 반환
 *   <li>만료 검증은 Domain에서 자동 처리 (Assembler의 getPresignedUrl 호출 시)
 * </ul>
 */
@Service
public class InitSingleUploadService implements InitSingleUploadUseCase {

    private static final Logger log = LoggerFactory.getLogger(InitSingleUploadService.class);

    private final SingleUploadAssembler singleUploadAssembler;
    private final FindUploadSessionQueryPort findUploadSessionQueryPort;
    private final UploadSessionFacade uploadSessionFacade;

    public InitSingleUploadService(
            SingleUploadAssembler singleUploadAssembler,
            FindUploadSessionQueryPort findUploadSessionQueryPort,
            UploadSessionFacade uploadSessionFacade) {
        this.singleUploadAssembler = singleUploadAssembler;
        this.findUploadSessionQueryPort = findUploadSessionQueryPort;
        this.uploadSessionFacade = uploadSessionFacade;
    }

    @Override
    public InitSingleUploadResponse execute(InitSingleUploadCommand command) {
        // 1. Command → Domain
        SingleUploadSession newSession = singleUploadAssembler.toDomain(command);

        // 2. 멱등성 키로 기존 세션 조회, 없으면 신규 생성
        SingleUploadSession session = findOrCreateSession(newSession);

        // 3. Response 변환 (getPresignedUrl 호출 시 만료 검증 자동 수행)
        return singleUploadAssembler.toResponse(session);
    }

    /**
     * IdempotencyKey로 기존 세션 조회, 없으면 신규 생성.
     *
     * <p><strong>동시성 처리</strong>:
     *
     * <ul>
     *   <li>DB에 (organization_id, idempotency_key) Unique 제약 존재
     *   <li>동시 요청 시 한쪽만 INSERT 성공, 나머지는 DataIntegrityViolationException
     *   <li>예외 발생 시 기존 세션 재조회하여 반환 (멱등성 보장)
     * </ul>
     */
    private SingleUploadSession findOrCreateSession(SingleUploadSession newSession) {
        return findUploadSessionQueryPort
                .findSingleUploadByIdempotencyKey(newSession.getIdempotencyKey())
                .orElseGet(() -> createSessionWithConcurrencyHandling(newSession));
    }

    private SingleUploadSession createSessionWithConcurrencyHandling(
            SingleUploadSession newSession) {
        try {
            return uploadSessionFacade.createAndActivateSingleUpload(newSession);
        } catch (DataIntegrityViolationException e) {
            // 동시 요청으로 인한 중복 키 예외 → 기존 세션 재조회
            log.info(
                    "Concurrent request detected for idempotencyKey: {}, fetching existing session",
                    newSession.getIdempotencyKey().getValue());
            return findUploadSessionQueryPort
                    .findSingleUploadByIdempotencyKey(newSession.getIdempotencyKey())
                    .orElseThrow(
                            () ->
                                    new IllegalStateException(
                                            "Session should exist after"
                                                    + " DataIntegrityViolationException"));
        }
    }
}
