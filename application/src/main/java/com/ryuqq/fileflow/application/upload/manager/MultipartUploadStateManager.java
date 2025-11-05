package com.ryuqq.fileflow.application.upload.manager;

import com.ryuqq.fileflow.application.upload.port.out.command.DeleteMultipartUploadPort;
import com.ryuqq.fileflow.application.upload.port.out.command.SaveMultipartUploadPort;
import com.ryuqq.fileflow.domain.upload.MultipartUpload;
import com.ryuqq.fileflow.domain.upload.UploadPart;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Multipart Upload State Manager
 *
 * <p>Multipart Upload 상태 관리를 전담하는 Manager 컴포넌트입니다.</p>
 *
 * <p><strong>책임:</strong></p>
 * <ul>
 *   <li>Multipart Upload 저장 (생성 및 업데이트)</li>
 *   <li>Multipart Upload 삭제</li>
 *   <li>상태 변경 메서드 (complete, abort, fail, addPart)</li>
 *   <li>트랜잭션 경계 관리 (Command 전담)</li>
 * </ul>
 *
 * <p><strong>설계 변경:</strong></p>
 * <ul>
 *   <li>✅ CQRS 적용: Command 전담 (Query 메서드 제거)</li>
 *   <li>✅ Port 분리: SaveMultipartUploadPort, DeleteMultipartUploadPort</li>
 *   <li>✅ StateManager 네이밍 (Manager → StateManager)</li>
 * </ul>
 *
 * @author Sangwon Ryu
 * @since 1.0.0
 */
@Component
public class MultipartUploadStateManager {

    private final SaveMultipartUploadPort savePort;
    private final DeleteMultipartUploadPort deletePort;

    /**
     * 생성자
     *
     * @param savePort Save Multipart Upload Port (Command)
     * @param deletePort Delete Multipart Upload Port (Command)
     */
    public MultipartUploadStateManager(
        SaveMultipartUploadPort savePort,
        DeleteMultipartUploadPort deletePort
    ) {
        this.savePort = savePort;
        this.deletePort = deletePort;
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
        return savePort.save(multipartUpload);
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
     * @param multipartUpload 완료할 Multipart Upload Domain Aggregate
     * @return 완료된 Multipart Upload
     * @throws IllegalStateException 완료 조건을 만족하지 않는 경우
     */
    @Transactional
    public MultipartUpload complete(MultipartUpload multipartUpload) {
        multipartUpload.complete();
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 중단 (Domain Aggregate 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.abort()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → ABORTED</p>
     *
     * @param multipartUpload 중단할 Multipart Upload Domain Aggregate
     * @return 중단된 Multipart Upload
     * @throws IllegalStateException 이미 완료된 경우
     */
    @Transactional
    public MultipartUpload abort(MultipartUpload multipartUpload) {
        multipartUpload.abort();
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 실패 처리 (Domain Aggregate 기반)
     *
     * <p><strong>처리 흐름:</strong></p>
     * <ol>
     *   <li>Domain 메서드 호출: multipartUpload.fail()</li>
     *   <li>상태 변경 사항 저장</li>
     * </ol>
     *
     * <p><strong>상태 변경:</strong> * → FAILED</p>
     *
     * @param multipartUpload 실패 처리할 Multipart Upload Domain Aggregate
     * @return 실패 처리된 Multipart Upload
     */
    @Transactional
    public MultipartUpload fail(MultipartUpload multipartUpload) {
        multipartUpload.fail();
        return savePort.save(multipartUpload);
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
     * @param multipartUpload 파트를 추가할 Multipart Upload Domain Aggregate
     * @param part 업로드된 파트 정보 (UploadPart Value Object)
     * @return 파트가 추가된 Multipart Upload
     * @throws IllegalArgumentException 파트 번호 중복 또는 범위 초과
     */
    @Transactional
    public MultipartUpload addPart(MultipartUpload multipartUpload, UploadPart part) {
        multipartUpload.addPart(part);
        return savePort.save(multipartUpload);
    }

    /**
     * Multipart Upload 삭제
     *
     * <p><strong>트랜잭션:</strong></p>
     * <ul>
     *   <li>트랜잭션 내에서 실행</li>
     * </ul>
     *
     * @param id Multipart Upload ID
     */
    @Transactional
    public void delete(Long id) {
        deletePort.delete(id);
    }
}

