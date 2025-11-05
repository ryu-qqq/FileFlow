package com.ryuqq.fileflow.application.download.port.in;

import com.ryuqq.fileflow.application.download.dto.response.ExternalDownloadResponse;

/**
 * Get Download Status Use Case
 * 외부 다운로드 상태 조회
 *
 * <p>책임:</p>
 * <ul>
 *   <li>Download ID로 ExternalDownload 조회</li>
 *   <li>현재 다운로드 진행 상태 반환</li>
 *   <li>상태: PENDING, DOWNLOADING, COMPLETED, FAILED</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface GetDownloadStatusUseCase {

    /**
     * 다운로드 상태 조회
     *
     * @param downloadId External Download ID
     * @return 다운로드 상태 응답
     * @throws IllegalArgumentException Download ID가 존재하지 않는 경우
     */
    ExternalDownloadResponse execute(Long downloadId);
}
