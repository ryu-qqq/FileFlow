package com.ryuqq.fileflow.domain.file.fixture;

import com.ryuqq.fileflow.domain.file.asset.FileId;

import java.util.List;
import java.util.stream.IntStream;

/**
 * FileId Test Fixture
 *
 * <p>테스트에서 FileId 인스턴스를 쉽게 생성하기 위한 Factory 클래스입니다.</p>
 *
 * <p><strong>사용 예시:</strong></p>
 * <pre>{@code
 * // 기본 ID (1L)
 * FileId id = FileIdFixture.create();
 *
 * // 특정 ID
 * FileId id = FileIdFixture.create(100L);
 *
 * // 여러 ID 생성
 * List<FileId> ids = FileIdFixture.createMultiple(5);
 * }</pre>
 *
 * @author Sangwon Ryu
 * @since 2025-11-02
 */
public class FileIdFixture {

    private static final Long DEFAULT_ID = 1L;

    /**
     * Private 생성자 - Utility 클래스 인스턴스화 방지
     *
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    private FileIdFixture() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 기본 FileId를 생성합니다 (ID = 1L).
     *
     * @return FileId 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static FileId create() {
        return new FileId(DEFAULT_ID);
    }

    /**
     * 특정 값으로 FileId를 생성합니다.
     *
     * @param value ID 값
     * @return FileId 인스턴스
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static FileId create(Long value) {
        return new FileId(value);
    }

    /**
     * 여러 개의 FileId를 생성합니다.
     *
     * <p>ID는 1부터 시작하는 연속된 값을 사용합니다.</p>
     *
     * @param count 생성할 ID 개수
     * @return FileId 리스트
     * @throws IllegalArgumentException count가 0 이하인 경우
     * @author Sangwon Ryu
     * @since 2025-11-02
     */
    public static List<FileId> createMultiple(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("count는 양수여야 합니다");
        }

        return IntStream.rangeClosed(1, count)
            .mapToObj(i -> new FileId((long) i))
            .toList();
    }
}
