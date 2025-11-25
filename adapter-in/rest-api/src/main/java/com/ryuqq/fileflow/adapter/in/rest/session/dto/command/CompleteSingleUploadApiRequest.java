package com.ryuqq.fileflow.adapter.in.rest.session.dto.command;

import jakarta.validation.constraints.NotBlank;

/**
 * 단일 파일 업로드 완료 API Request.
 *
 * <p>S3에 업로드 완료 후 세션 완료 처리를 요청합니다.
 *
 * @param etag S3가 반환한 ETag
 * @author development-team
 * @since 1.0.0
 */
public record CompleteSingleUploadApiRequest(@NotBlank(message = "ETag는 필수입니다") String etag) {}
