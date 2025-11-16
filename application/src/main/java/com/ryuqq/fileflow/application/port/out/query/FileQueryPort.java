package com.ryuqq.fileflow.application.port.out.query;

import com.ryuqq.fileflow.domain.aggregate.File;
import com.ryuqq.fileflow.domain.vo.FileId;
import com.ryuqq.fileflow.domain.vo.FileSearchCriteria;

import java.util.List;
import java.util.Optional;

/**
 * File Query Port (Outbound Port)
 * <p>
 * Zero-Tolerance 규칙 준수:
 * - 인터페이스명: *QueryPort
 * - 패키지: ..application..port.out.query..
 * - 필수 메서드 4개: findById, existsById, findByCriteria, countByCriteria
 * - Value Object 파라미터: FileId, FileSearchCriteria
 * - Domain 반환: File Aggregate (DTO/Entity 반환 금지)
 * </p>
 * <p>
 * Application Layer에서 Persistence Layer로 File Aggregate 조회 요청을 위한 Port입니다.
 * CQRS 분리: 조회 전용 Port (저장/수정/삭제 메서드 금지)
 * </p>
 */
public interface FileQueryPort {

    /**
     * ID로 파일 조회
     *
     * @param id 파일 ID (FileId VO)
     * @return 조회된 File Aggregate (Optional)
     */
    Optional<File> findById(FileId id);

    /**
     * ID로 파일 존재 여부 확인
     *
     * @param id 파일 ID (FileId VO)
     * @return 존재 여부 (boolean)
     */
    boolean existsById(FileId id);

    /**
     * 검색 조건으로 파일 목록 조회
     *
     * @param criteria 검색 조건 (FileSearchCriteria VO)
     * @return 조회된 File Aggregate 목록
     */
    List<File> findByCriteria(FileSearchCriteria criteria);

    /**
     * 검색 조건으로 파일 개수 조회
     *
     * @param criteria 검색 조건 (FileSearchCriteria VO)
     * @return 파일 개수
     */
    long countByCriteria(FileSearchCriteria criteria);
}
