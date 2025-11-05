package com.ryuqq.fileflow.application.file.service;

import com.ryuqq.fileflow.application.file.dto.query.ListFilesQuery;
import com.ryuqq.fileflow.application.file.dto.response.FileListResponse;
import com.ryuqq.fileflow.application.file.dto.response.FileMetadataResponse;
import com.ryuqq.fileflow.application.file.port.in.GetFilesUseCase;
import com.ryuqq.fileflow.application.file.port.out.FileQueryPort;
import com.ryuqq.fileflow.domain.file.asset.FileAsset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 파일 목록 조회 Service
 *
 * <p>CQRS Query Side - 페이징 및 필터링이 적용된 파일 목록 조회 구현</p>
 *
 * <p><strong>트랜잭션 전략:</strong></p>
 * <ul>
 *   <li>@Transactional(readOnly = true) - 읽기 전용 최적화</li>
 *   <li>Dirty Checking 비활성화</li>
 *   <li>FlushMode = MANUAL</li>
 * </ul>
 *
 * <p><strong>실행 흐름:</strong></p>
 * <ol>
 *   <li>FileQueryPort를 통해 파일 목록 조회</li>
 *   <li>전체 개수 조회 (페이징 정보)</li>
 *   <li>Domain 객체 리스트 → Response DTO 리스트 변환</li>
 *   <li>페이징 정보 포함 응답 생성</li>
 * </ol>
 *
 * <p><strong>성능 최적화:</strong></p>
 * <ul>
 *   <li>인덱스 활용: (tenant_id, organization_id, uploaded_at)</li>
 *   <li>필요한 필드만 조회 (DTO 프로젝션)</li>
 *   <li>N+1 문제 방지 (Fetch Join)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Service
public class GetFilesService implements GetFilesUseCase {

    private final FileQueryPort fileQueryPort;

    /**
     * 생성자
     *
     * @param fileQueryPort 파일 조회 Port
     */
    public GetFilesService(FileQueryPort fileQueryPort) {
        this.fileQueryPort = fileQueryPort;
    }

    /**
     * 파일 목록 조회
     *
     * @param query 파일 목록 조회 Query
     * @return 페이징된 파일 목록 응답
     */
    @Transactional(readOnly = true)
    @Override
    public FileListResponse execute(ListFilesQuery query) {
        // 1. 파일 목록 조회
        List<FileAsset> fileAssets = fileQueryPort.findAllByQuery(query);

        // 2. 전체 개수 조회
        long totalElements = fileQueryPort.countByQuery(query);

        // 3. Domain 객체 → Response DTO 변환
        List<FileMetadataResponse> content = fileAssets.stream()
            .map(FileMetadataResponse::from)
            .collect(Collectors.toList());

        // 4. 페이징 응답 생성
        return FileListResponse.of(
            content,
            query.page(),
            query.size(),
            totalElements
        );
    }
}
