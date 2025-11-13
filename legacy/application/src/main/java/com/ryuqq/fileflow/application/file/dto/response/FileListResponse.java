package com.ryuqq.fileflow.application.file.dto.response;

import java.util.List;

/**
 * 파일 목록 응답 DTO
 *
 * <p>CQRS Query Side - 페이징된 파일 목록 응답</p>
 *
 * <p><strong>페이징 정보 포함:</strong></p>
 * <ul>
 *   <li>현재 페이지 번호</li>
 *   <li>페이지 크기</li>
 *   <li>전체 요소 수</li>
 *   <li>전체 페이지 수</li>
 *   <li>다음 페이지 존재 여부</li>
 * </ul>
 *
 * @param content 파일 메타데이터 목록
 * @param page 현재 페이지 번호 (0부터 시작)
 * @param size 페이지 크기
 * @param totalElements 전체 요소 수
 * @param totalPages 전체 페이지 수
 * @param hasNext 다음 페이지 존재 여부
 * @author Sangwon Ryu
 * @since 1.0.0
 */
public record FileListResponse(
    List<FileMetadataResponse> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {

    /**
     * Static Factory Method
     *
     * @param content 파일 메타데이터 목록
     * @param page 현재 페이지 번호
     * @param size 페이지 크기
     * @param totalElements 전체 요소 수
     * @return FileListResponse
     */
    public static FileListResponse of(
        List<FileMetadataResponse> content,
        int page,
        int size,
        long totalElements
    ) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        boolean hasNext = page < totalPages - 1;

        return new FileListResponse(
            content,
            page,
            size,
            totalElements,
            totalPages,
            hasNext
        );
    }
}
