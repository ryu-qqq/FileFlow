package com.ryuqq.fileflow.adapter.in.rest.session.mapper;

import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.AddCompletedPartApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CompleteSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateMultipartUploadSessionApiRequest;
import com.ryuqq.fileflow.adapter.in.rest.session.dto.command.CreateSingleUploadSessionApiRequest;
import com.ryuqq.fileflow.application.session.dto.command.AbortMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.AddCompletedPartCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CompleteSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateMultipartUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.CreateSingleUploadSessionCommand;
import com.ryuqq.fileflow.application.session.dto.command.GeneratePresignedPartUrlCommand;
import org.springframework.stereotype.Component;

/**
 * SessionCommandApiMapper - 업로드 세션 Command Mapper.
 *
 * <p>API Request → Application Command 변환을 담당합니다.
 *
 * <p>API-MAP-001: Mapper는 @Component로 등록.
 *
 * <p>API-MAP-004: Command Mapper는 toCommand() 메서드 제공.
 *
 * <p>API-MAP-003: 순수 변환 로직만.
 */
@Component
public class SessionCommandApiMapper {

    /**
     * CreateSingleUploadSessionApiRequest → CreateSingleUploadSessionCommand 변환.
     *
     * @param request API 요청
     * @return CreateSingleUploadSessionCommand
     */
    public CreateSingleUploadSessionCommand toCommand(CreateSingleUploadSessionApiRequest request) {
        return new CreateSingleUploadSessionCommand(
                request.fileName(),
                request.contentType(),
                request.accessType(),
                request.purpose(),
                request.source());
    }

    /**
     * CompleteSingleUploadSessionApiRequest → CompleteSingleUploadSessionCommand 변환.
     *
     * @param sessionId 세션 ID (PathVariable)
     * @param request API 요청
     * @return CompleteSingleUploadSessionCommand
     */
    public CompleteSingleUploadSessionCommand toCommand(
            String sessionId, CompleteSingleUploadSessionApiRequest request) {
        return new CompleteSingleUploadSessionCommand(
                sessionId, request.fileSize(), request.etag());
    }

    /**
     * CreateMultipartUploadSessionApiRequest → CreateMultipartUploadSessionCommand 변환.
     *
     * @param request API 요청
     * @return CreateMultipartUploadSessionCommand
     */
    public CreateMultipartUploadSessionCommand toCommand(
            CreateMultipartUploadSessionApiRequest request) {
        return new CreateMultipartUploadSessionCommand(
                request.fileName(),
                request.contentType(),
                request.accessType(),
                request.partSize(),
                request.purpose(),
                request.source());
    }

    /**
     * Path Variables → GeneratePresignedPartUrlCommand 변환.
     *
     * @param sessionId 세션 ID (PathVariable)
     * @param partNumber 파트 번호 (PathVariable)
     * @return GeneratePresignedPartUrlCommand
     */
    public GeneratePresignedPartUrlCommand toCommand(String sessionId, int partNumber) {
        return new GeneratePresignedPartUrlCommand(sessionId, partNumber);
    }

    /**
     * AddCompletedPartApiRequest → AddCompletedPartCommand 변환.
     *
     * @param sessionId 세션 ID (PathVariable)
     * @param request API 요청
     * @return AddCompletedPartCommand
     */
    public AddCompletedPartCommand toCommand(String sessionId, AddCompletedPartApiRequest request) {
        return new AddCompletedPartCommand(
                sessionId, request.partNumber(), request.etag(), request.size());
    }

    /**
     * CompleteMultipartUploadSessionApiRequest → CompleteMultipartUploadSessionCommand 변환.
     *
     * @param sessionId 세션 ID (PathVariable)
     * @param request API 요청
     * @return CompleteMultipartUploadSessionCommand
     */
    public CompleteMultipartUploadSessionCommand toCommand(
            String sessionId, CompleteMultipartUploadSessionApiRequest request) {
        return new CompleteMultipartUploadSessionCommand(
                sessionId, request.totalFileSize(), request.etag());
    }

    /**
     * Path Variable → AbortMultipartUploadSessionCommand 변환.
     *
     * @param sessionId 세션 ID (PathVariable)
     * @return AbortMultipartUploadSessionCommand
     */
    public AbortMultipartUploadSessionCommand toAbortCommand(String sessionId) {
        return new AbortMultipartUploadSessionCommand(sessionId);
    }
}
