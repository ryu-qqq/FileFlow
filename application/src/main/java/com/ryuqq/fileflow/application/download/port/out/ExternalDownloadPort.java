package com.ryuqq.fileflow.application.download.port.out;

import com.ryuqq.fileflow.domain.download.ExternalDownload;
import com.ryuqq.fileflow.domain.download.ExternalDownloadStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * External Download Port (Out)
 *
 * <p>Application Layer에서 Persistence Layer로 나가는 Port 인터페이스입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>External Download Aggregate의 영속화 인터페이스 정의</li>
 *   <li>Adapter 구현체와 Application Layer 간 계약</li>
 *   <li>도메인 용어 사용 (JPA/DB 용어 금지)</li>
 * </ul>
 *
 * <p><strong>설계 원칙:</strong></p>
 * <ul>
 *   <li>✅ Port & Adapter 패턴의 Port 역할</li>
 *   <li>✅ Domain 객체만 사용 (Entity, DTO 금지)</li>
 *   <li>✅ 비즈니스 의미 있는 메서드명</li>
 *   <li>✅ Infrastructure 독립적</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public interface ExternalDownloadPort {

    /**
     * External Download 저장
     *
     * <p>신규 생성 또는 기존 데이터 업데이트를 수행합니다.</p>
     *
     * @param download External Download Domain Aggregate
     * @return 저장된 External Download (ID 포함)
     */
    ExternalDownload save(ExternalDownload download);

    /**
     * ID로 External Download 조회
     *
     * @param id External Download ID
     * @return External Download (Optional)
     */
    Optional<ExternalDownload> findById(Long id);

    /**
     * Upload Session ID로 External Download 조회
     *
     * @param uploadSessionId Upload Session ID
     * @return External Download (Optional)
     */
    Optional<ExternalDownload> findByUploadSessionId(Long uploadSessionId);

    /**
     * 상태별 External Download 목록 조회
     *
     * @param status External Download 상태
     * @return External Download 목록
     */
    List<ExternalDownload> findByStatus(ExternalDownloadStatus status);

    /**
     * 재시도 가능한 다운로드 목록 조회
     *
     * <p>다음 조건을 만족하는 다운로드를 조회합니다:</p>
     * <ul>
     *   <li>DOWNLOADING 상태</li>
     *   <li>재시도 횟수가 최대값 미만</li>
     *   <li>마지막 재시도 시간이 retryAfter 이전</li>
     * </ul>
     *
     * @param maxRetry 최대 재시도 횟수
     * @param retryAfter 재시도 가능 시간 (이 시간 이전에 마지막 재시도한 것만)
     * @return 재시도 가능한 External Download 목록
     */
    List<ExternalDownload> findRetryableDownloads(Integer maxRetry, LocalDateTime retryAfter);

    /**
     * External Download 삭제
     *
     * @param id External Download ID
     */
    void delete(Long id);
}
