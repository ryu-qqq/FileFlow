package com.ryuqq.fileflow.application.download.manager.cache;

import com.ryuqq.fileflow.application.download.port.out.cache.DownloadUrlBlacklistPort;
import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DownloadUrlBlacklistManager {

    private static final Logger log = LoggerFactory.getLogger(DownloadUrlBlacklistManager.class);

    private static final Duration DEFAULT_BLACKLIST_TTL = Duration.ofHours(1);

    private final DownloadUrlBlacklistPort downloadUrlBlacklistPort;

    public DownloadUrlBlacklistManager(DownloadUrlBlacklistPort downloadUrlBlacklistPort) {
        this.downloadUrlBlacklistPort = downloadUrlBlacklistPort;
    }

    public void registerBlacklist(String sourceUrl, String reason) {
        try {
            downloadUrlBlacklistPort.register(sourceUrl, reason, DEFAULT_BLACKLIST_TTL);
            log.info("URL 블랙리스트 등록: sourceUrl={}, reason={}", sourceUrl, reason);
        } catch (Exception e) {
            log.warn("URL 블랙리스트 등록 실패 (무시): sourceUrl={}, error={}", sourceUrl, e.getMessage());
        }
    }

    public boolean isBlacklisted(String sourceUrl) {
        try {
            return downloadUrlBlacklistPort.isBlacklisted(sourceUrl);
        } catch (Exception e) {
            log.warn("URL 블랙리스트 조회 실패 (통과 처리): sourceUrl={}, error={}", sourceUrl, e.getMessage());
            return false;
        }
    }
}
