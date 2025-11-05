package com.ryuqq.fileflow.adapter.rest.file.fixture;

import com.ryuqq.fileflow.adapter.rest.file.dto.request.ListFilesApiRequest;

/**
 * ListFilesApiRequest 테스트 Fixture
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 * @see ListFilesApiRequest
 */
public class ListFilesApiRequestFixture {

    /**
     * 기본값으로 ListFilesApiRequest 생성
     *
     * @return 기본값을 가진 ListFilesApiRequest
     */
    public static ListFilesApiRequest create() {
        return new ListFilesApiRequest();
    }

    /**
     * 특정 소유자 ID로 ListFilesApiRequest 생성
     *
     * @param ownerUserId 소유자 User ID
     * @return 지정된 소유자 ID를 가진 ListFilesApiRequest
     */
    public static ListFilesApiRequest createWithOwner(Long ownerUserId) {
        ListFilesApiRequest request = new ListFilesApiRequest();
        request.setOwnerUserId(ownerUserId);
        return request;
    }

    /**
     * 특정 상태로 ListFilesApiRequest 생성
     *
     * @param status 파일 상태
     * @return 지정된 상태를 가진 ListFilesApiRequest
     */
    public static ListFilesApiRequest createWithStatus(String status) {
        ListFilesApiRequest request = new ListFilesApiRequest();
        request.setStatus(status);
        return request;
    }

    /**
     * 모든 필터 조건으로 ListFilesApiRequest 생성
     *
     * @param ownerUserId 소유자 User ID
     * @param status 파일 상태
     * @param visibility 가시성
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 모든 조건을 가진 ListFilesApiRequest
     */
    public static ListFilesApiRequest createWith(
        Long ownerUserId,
        String status,
        String visibility,
        Integer page,
        Integer size
    ) {
        ListFilesApiRequest request = new ListFilesApiRequest();
        request.setOwnerUserId(ownerUserId);
        request.setStatus(status);
        request.setVisibility(visibility);
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    // Private 생성자
    private ListFilesApiRequestFixture() {
        throw new AssertionError("Fixture 클래스는 인스턴스화할 수 없습니다.");
    }
}
