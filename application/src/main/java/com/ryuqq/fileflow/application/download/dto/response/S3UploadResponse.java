package com.ryuqq.fileflow.application.download.dto.response;

import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.FileName;
import com.ryuqq.fileflow.domain.session.vo.S3Key;

/**
 * S3 업로드 준비 정보 응답.
 *
 * <p>ExternalDownload 처리 시 HTTP 다운로드 결과를 기반으로 S3 업로드에 필요한 정보를 조합한 응답입니다.
 *
 * @param s3Key S3 객체 키
 * @param fileName 파일명
 * @param contentType 컨텐츠 타입
 * @param content 파일 콘텐츠 (바이트 배열)
 */
public record S3UploadResponse(
        S3Key s3Key, FileName fileName, ContentType contentType, byte[] content) {}
