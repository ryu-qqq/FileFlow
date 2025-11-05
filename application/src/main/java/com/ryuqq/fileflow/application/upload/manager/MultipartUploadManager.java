package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.MultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import com.ryuqq.fileflow.domain.upload.UploadSessionId;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Multipart Upload Manager
 *
 * <p>Multipart Upload 상태 관리를 전담하는 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload 저장 (생성 및 업데이트)</li>
 *   <li>Multipart Upload 조회 (ID, Upload Session ID)</li>
 *   <li>상태 변경 메서드 (initiate, addPart, complete, abort, fail)</li>
 *   <li>트랜잭션 경계 관리</li>
 * </ul>
 *
 * <p><strong>사용 시나리오:</strong></p>
 * <ul>
 *   <li>InitMultipartUploadService: Multipart 저장</li>
 *   <li>UploadPartService: 파트 업로드 진행</li>
 *   <li>CompleteMultipartUploadService: 업로드 완료 처리</li>
 *   <li>AbortMultipartUploadService: 업로드 중단 처리</li>
 * </ul>
 *
 * <p><strong>트랜잭션:</strong></p>
 * <ul>
 *   <li>조회 메서드: readOnly=true</li>
 *   <li>상태 변경 메서드: readOnly=false (기본값)</li>
 * </ul>
 *
 * <p><strong>패턴:</strong></p>
 * <ul>
 *   <li>Manager Pattern: 도메인 객체의 생명주기 관리</li>
 *   <li>Transaction Script Pattern: 트랜잭션 경계 명확화</li>
 *   <li>Facade Pattern: MultipartUploadPort 캡슐화</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadManager {

    private final MultipartUploadPort multipartUploadPort;

    /**
     * 생성자
     *
     * @param multipartUploadPort Multipart Upload Port
     */
    public MultipartUploadManager(MultipartUploadPort multipartUploadPort) {
        this.multipartUploadPort = multipartUploadPort;
    }

    /**
     * Multipart Upload 저장
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>신규 생성 또는 기존 데이터 업데이트</li>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param multipartUpload Multipart Upload Domain Aggregate
     * @return 저장된 Multipart Upload (ID 포함)
     */
    @Transactional
    public MultipartUpload save(MultipartUpload multipartUpload) {
        return multipartUploadPort.save(multipartUpload);
    }

    /**
     * ID로 Multipart Upload 조회
     *
     * @param id Multipart Upload ID
     * @return Multipart Upload (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<MultipartUpload> findById(Long id) {
        return multipartUploadPort.findById(id);
    }

    /**
     * Upload Session ID로 Multipart Upload 조회
     *
     * @param uploadSessionId Upload Session ID (Long 타입)
     * @return Multipart Upload (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<MultipartUpload> findByUploadSessionId(Long uploadSessionId) {
        return multipartUploadPort.findByUploadSessionId(uploadSessionId);
    }

    /**
     * Upload Session ID로 Multipart Upload 조회 (Value Object 지원)
     *
     * <p>UploadSessionId Value Object를 직접 받아 조회합니다.</p>
     *
     * @param uploadSessionId UploadSession의 ID (UploadSessionId Value Object)
     * @return Multipart Upload (Optional)
     */
    @Transactional(readOnly = true)
    public Optional<MultipartUpload> findByUploadSessionId(UploadSessionId uploadSessionId) {
        return multipartUploadPort.findByUploadSessionId(uploadSessionId.value());
    }

    /**
     * Multipart Upload 완료 (ID 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Multipart Upload 조회</li>
     *   <li>Domain 메서드 호출: multipartUpload.complete()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> IN_PROGRESS → COMPLETED</p>
     *
     * @param id Multipart Upload ID
     * @return 완료된 Multipart Upload
     * @throws IllegalArgumentException Multipart Upload가 존재하지 않는 경우
     * @throws IllegalStateException 완료 조건을 만족하지 않는 경우
     */
    @Transactional
    public MultipartUpload complete(Long id) {
        MultipartUpload multipartUpload = multipartUploadPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Multipart Upload not found: " + id
            ));

        multipartUpload.complete();

        return multipartUploadPort.save(multipartUpload);
    }

    /**
     * Multipart Upload 완료 (Domain Aggregate 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.complete()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> IN_PROGRESS → COMPLETED</p>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>CompleteMultipartUploadService: 이미 조회된 MultipartUpload 완료</li>
     * </ul>
     *
     * @param multipartUpload 완료할 Multipart Upload Domain Aggregate
     * @return 완료된 Multipart Upload
     * @throws IllegalStateException 완료 조건을 만족하지 않는 경우
     */
    @Transactional
    public MultipartUpload complete(MultipartUpload multipartUpload) {
        multipartUpload.complete();
        return multipartUploadPort.save(multipartUpload);
    }

    /**
     * Multipart Upload 중단
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Multipart Upload 조회</li>
     *   <li>Domain 메서드 호출: multipartUpload.abort()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → ABORTED</p>
     *
     * @param id Multipart Upload ID
     * @return 중단된 Multipart Upload
     * @throws IllegalArgumentException Multipart Upload가 존재하지 않는 경우
     * @throws IllegalStateException 이미 완료된 경우
     */
    @Transactional
    public MultipartUpload abort(Long id) {
        MultipartUpload multipartUpload = multipartUploadPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Multipart Upload not found: " + id
            ));

        multipartUpload.abort();

        return multipartUploadPort.save(multipartUpload);
    }

    /**
     * Multipart Upload 실패 처리
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Multipart Upload 조회</li>
     *   <li>Domain 메서드 호출: multipartUpload.fail()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → FAILED</p>
     *
     * @param id Multipart Upload ID
     * @return 실패 처리된 Multipart Upload
     * @throws IllegalArgumentException Multipart Upload가 존재하지 않는 경우
     */
    @Transactional
    public MultipartUpload fail(Long id) {
        MultipartUpload multipartUpload = multipartUploadPort.findById(id)
            .orElseThrow(() -> new IllegalArgumentException(
                "Multipart Upload not found: " + id
            ));

        multipartUpload.fail();

        return multipartUploadPort.save(multipartUpload);
    }

    /**
     * Multipart Upload에 파트 추가
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.addPart(part)</li>
     *   <li>파트 추가 사항 저장</li>
     * </ol>
     *
     * <p><strong>비즈니스 규칙:</strong></p>
     * <ul>
     *   <li>파트 번호 중복 검증 (Domain 계층에서 수행)</li>
     *   <li>전체 파트 수 제한 검증 (Domain 계층에서 수행)</li>
     * </ul>
     *
     * <p><strong>사용 시나리오:</strong></p>
     * <ul>
     *   <li>MarkPartUploadedService: 클라이언트가 파트 업로드 완료 알림 시</li>
     * </ul>
     *
     * @param multipartUpload 파트를 추가할 Multipart Upload Domain Aggregate
     * @param part 업로드된 파트 정보 (UploadPart Value Object)
     * @return 파트가 추가된 Multipart Upload
     * @throws IllegalArgumentException 파트 번호 중복 또는 범위 초과
     */
    @Transactional
    public MultipartUpload addPart(MultipartUpload multipartUpload, UploadPart part) {
        multipartUpload.addPart(part);
        return multipartUploadPort.save(multipartUpload);
    }
}
