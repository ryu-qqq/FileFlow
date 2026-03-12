package com.ryuqq.fileflow.application.download.port.out.cache;

import java.time.Duration;

/**
 * 다운로드 URL 블랙리스트 캐시 포트.
 *
 * <p>영구 실패(403/404 등)한 URL을 일정 기간 캐싱하여 동일 URL의 재시도를 방지합니다.
 */
public interface DownloadUrlBlacklistPort {

    void register(String sourceUrl, String reason, Duration ttl);

    boolean isBlacklisted(String sourceUrl);
}
