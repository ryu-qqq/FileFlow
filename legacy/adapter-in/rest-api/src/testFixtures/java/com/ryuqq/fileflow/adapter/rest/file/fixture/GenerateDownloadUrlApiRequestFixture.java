package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.request.GenerateDownloadUrlApiRequest;

/**
 * GenerateDownloadUrlApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see GenerateDownloadUrlApiRequest
 */
public class GenerateDownloadUrlApiRequestFixture {

    /**
     * 기본값으로 GenerateDownloadUrlApiRequest 생성 (fileId: 1L, expirationHours: 1)
     *
     * @return 기본값을 가진 GenerateDownloadUrlApiRequest
     */
    public static GenerateDownloadUrlApiRequest create() {
        GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest();
        request.setFileId(1L);
        request.setExpirationHours(1);
        return request;
    }

    /**
     * 특정 File ID로 GenerateDownloadUrlApiRequest 생성
     *
     * @param fileId File ID
     * @return 지정된 File ID를 가진 GenerateDownloadUrlApiRequest
     */
    public static GenerateDownloadUrlApiRequest createWithFileId(Long fileId) {
        GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest();
        request.setFileId(fileId);
        request.setExpirationHours(1);
        return request;
    }

    /**
     * 모든 필드로 GenerateDownloadUrlApiRequest 생성
     *
     * @param fileId File ID
     * @param expirationHours 만료 시간 (시간 단위)
     * @return 모든 필드를 가진 GenerateDownloadUrlApiRequest
     */
    public static GenerateDownloadUrlApiRequest createWith(Long fileId, Integer expirationHours) {
        GenerateDownloadUrlApiRequest request = new GenerateDownloadUrlApiRequest();
        request.setFileId(fileId);
        request.setExpirationHours(expirationHours);
        return request;
    }

    // Private 생성자
    private GenerateDownloadUrlApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
