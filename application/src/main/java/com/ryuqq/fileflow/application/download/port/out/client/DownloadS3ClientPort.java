package com.ryuqq.fileflow.application.download.port.out.client;

import com.ryuqq.fileflow.domain.session.vo.ContentType;
import com.ryuqq.fileflow.domain.session.vo.ETag;
import com.ryuqq.fileflow.domain.session.vo.S3Bucket;
import com.ryuqq.fileflow.domain.session.vo.S3Key;

/**
 * 외부 다운로드 전용 S3 클라이언트 포트.
 *
 * <p>외부에서 다운로드한 파일을 S3에 업로드할 때 사용합니다.
 *
 * <p><strong>사용처</strong>:
 *
 * <ul>
 *   <li>ExternalDownloadProcessingFacade - 외부 이미지 다운로드 후 S3 업로드
 * </ul>
 */
public interface DownloadS3ClientPort {

    /**
     * 바이트 배열을 S3에 직접 업로드합니다.
     *
     * <p>외부 다운로드 Worker에서 다운로드한 이미지를 S3에 업로드할 때 사용합니다.
     *
     * @param bucket S3 버킷
     * @param s3Key S3 객체 키
     * @param contentType Content-Type
     * @param data 업로드할 바이트 배열
     * @return 업로드된 객체의 ETag
     */
    ETag putObject(S3Bucket bucket, S3Key s3Key, ContentType contentType, byte[] data);
}
