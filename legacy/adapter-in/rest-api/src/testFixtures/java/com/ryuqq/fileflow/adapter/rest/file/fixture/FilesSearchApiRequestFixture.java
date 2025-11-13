package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.request.FilesSearchApiRequest;

/**
 * FilesSearchApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see FilesSearchApiRequest
 */
public class FilesSearchApiRequestFixture {

    /**
     * 기본값으로 FilesSearchApiRequest 생성
     *
     * @return 기본값을 가진 FilesSearchApiRequest
     */
    public static FilesSearchApiRequest create() {
        FilesSearchApiRequest request = new FilesSearchApiRequest();
        return request;
    }

    /**
     * 특정 소유자 ID로 FilesSearchApiRequest 생성
     *
     * @param ownerUserId 소유자 User ID
     * @return 지정된 소유자 ID를 가진 FilesSearchApiRequest
     */
    public static FilesSearchApiRequest createWithOwner(Long ownerUserId) {
        FilesSearchApiRequest request = new FilesSearchApiRequest();
        request.setOwnerUserId(ownerUserId);
        return request;
    }

    /**
     * 특정 상태로 FilesSearchApiRequest 생성
     *
     * @param status 파일 상태
     * @return 지정된 상태를 가진 FilesSearchApiRequest
     */
    public static FilesSearchApiRequest createWithStatus(String status) {
        FilesSearchApiRequest request = new FilesSearchApiRequest();
        request.setStatus(status);
        return request;
    }

    /**
     * 모든 필터 조건으로 FilesSearchApiRequest 생성
     *
     * @param ownerUserId 소유자 User ID
     * @param status 파일 상태
     * @param visibility 가시성
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 모든 조건을 가진 FilesSearchApiRequest
     */
    public static FilesSearchApiRequest createWith(
        Long ownerUserId,
        String status,
        String visibility,
        Integer page,
        Integer size
    ) {
        FilesSearchApiRequest request = new FilesSearchApiRequest();
        request.setOwnerUserId(ownerUserId);
        request.setStatus(status);
        request.setVisibility(visibility);
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    // Private 생성자
    private FilesSearchApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
