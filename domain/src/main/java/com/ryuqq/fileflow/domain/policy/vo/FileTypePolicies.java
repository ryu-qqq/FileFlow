package com.ryuqq.fileflow.domain.policy.vo;

import java.util.Objects;
import com.ryuqq.fileflow.domain.policy.FileType;

/**
 * FileTypePolicies - 파일 타입별 정책을 그룹화하는 복합 Value Object
 *
 * 목적:
 * - 다양한 파일 타입(IMAGE, VIDEO, HTML, EXCEL, PDF)의 정책을 하나의 응집된 객체로 관리
 * - 정책 존재 여부 검증 및 타입별 정책 조회 기능 제공
 * - DDD Aggregate 내에서 정책 컬렉션의 Value Object 역할
 *
 * 설계 원칙:
 * - Immutability: 생성 후 상태 변경 불가
 * - Self-Validation: 최소 1개 이상의 정책 필수
 * - Type-Safe Access: FileType Enum을 통한 안전한 정책 조회
 *
 * 사용 예시:
 * <pre>{@code
 * FileTypePolicies policies = FileTypePolicies.of(
 *     ImagePolicy.of(...),
 *     VideoPolicy.of(...),
 *     HtmlPolicy.of(...),
 *     null,  // excelPolicy 없음
 *     null   // pdfPolicy 없음
 * );
 *
 * if (policies.hasPolicyFor(FileType.IMAGE)) {
 *     ImagePolicy imagePolicy = (ImagePolicy) policies.getPolicyFor(FileType.IMAGE);
 * }
 * }</pre>
 *
 * 불변성 보장:
 * - final 필드 + private 생성자
 * - 팩토리 메서드를 통한 생성
 * - nullable 정책 허용 (최소 1개만 존재하면 됨)
 */
public final class FileTypePolicies {

    private final ImagePolicy imagePolicy;
    private final VideoPolicy videoPolicy;
    private final HtmlPolicy htmlPolicy;
    private final ExcelPolicy excelPolicy;
    private final PdfPolicy pdfPolicy;

    private FileTypePolicies(
        ImagePolicy imagePolicy,
        VideoPolicy videoPolicy,
        HtmlPolicy htmlPolicy,
        ExcelPolicy excelPolicy,
        PdfPolicy pdfPolicy
    ) {
        validateAtLeastOnePolicy(imagePolicy, videoPolicy, htmlPolicy, excelPolicy, pdfPolicy);

        this.imagePolicy = imagePolicy;
        this.videoPolicy = videoPolicy;
        this.htmlPolicy = htmlPolicy;
        this.excelPolicy = excelPolicy;
        this.pdfPolicy = pdfPolicy;
    }

    /**
     * FileTypePolicies 생성 팩토리 메서드
     *
     * @param imagePolicy 이미지 정책 (nullable)
     * @param videoPolicy 비디오 정책 (nullable)
     * @param htmlPolicy HTML 정책 (nullable)
     * @param excelPolicy Excel 정책 (nullable)
     * @param pdfPolicy PDF 정책 (nullable)
     * @return 검증된 FileTypePolicies 인스턴스
     * @throws IllegalArgumentException 모든 정책이 null인 경우
     */
    public static FileTypePolicies of(
        ImagePolicy imagePolicy,
        VideoPolicy videoPolicy,
        HtmlPolicy htmlPolicy,
        ExcelPolicy excelPolicy,
        PdfPolicy pdfPolicy
    ) {
        return new FileTypePolicies(imagePolicy, videoPolicy, htmlPolicy, excelPolicy, pdfPolicy);
    }

    /**
     * 특정 파일 타입의 정책 존재 여부 확인
     *
     * @param fileType 확인할 파일 타입
     * @return 해당 타입의 정책이 존재하면 true, 없으면 false
     * @throws IllegalArgumentException fileType이 null인 경우
     */
    public boolean hasPolicyFor(FileType fileType) {
        validateFileType(fileType);
        return getPolicyFor(fileType) != null;
    }

    /**
     * 특정 파일 타입의 정책 조회
     *
     * 비즈니스 로직:
     * - FileType에 따라 해당하는 정책 객체 반환
     * - 정책이 없으면 null 반환 (호출자가 hasPolicyFor()로 사전 확인 권장)
     *
     * @param fileType 조회할 파일 타입
     * @return 해당 타입의 정책 객체, 없으면 null
     * @throws IllegalArgumentException fileType이 null인 경우
     */
    public Object getPolicyFor(FileType fileType) {
        validateFileType(fileType);

        return switch (fileType) {
            case IMAGE -> imagePolicy;
            case VIDEO -> videoPolicy;
            case HTML -> htmlPolicy;
            case EXCEL -> excelPolicy;
            case PDF -> pdfPolicy;
        };
    }

    /**
     * 파일 타입에 따라 정책을 검증합니다.
     *
     * @param fileType 파일 타입
     * @param attributes 파일 속성
     * @throws IllegalArgumentException 정책 위반 시
     */
    public void validate(FileType fileType, FileAttributes attributes) {
        validateFileType(fileType);
        Objects.requireNonNull(attributes, "FileAttributes must not be null");

        switch (fileType) {
            case IMAGE:
                if (imagePolicy == null) {
                    throw new IllegalArgumentException("No IMAGE policy configured");
                }
                imagePolicy.validate(attributes.format(), attributes.sizeBytes(), attributes.dimension());
                break;

            case VIDEO:
                if (videoPolicy == null) {
                    throw new IllegalArgumentException("No VIDEO policy configured");
                }
                videoPolicy.validate(attributes.format(), attributes.sizeBytes(), attributes.durationSeconds());
                break;

            case HTML:
                if (htmlPolicy == null) {
                    throw new IllegalArgumentException("No HTML policy configured");
                }
                htmlPolicy.validate(attributes.sizeBytes(), attributes.imageCount() != null ? attributes.imageCount() : 0);
                break;

            case EXCEL:
                if (excelPolicy == null) {
                    throw new IllegalArgumentException("No EXCEL policy configured");
                }
                excelPolicy.validate(attributes.sizeBytes(), attributes.sheetCount() != null ? attributes.sheetCount() : 1);
                break;

            case PDF:
                if (pdfPolicy == null) {
                    throw new IllegalArgumentException("No PDF policy configured");
                }
                pdfPolicy.validate(attributes.sizeBytes(), attributes.pageCount() != null ? attributes.pageCount() : 1);
                break;

            default:
                throw new IllegalArgumentException("Unsupported FileType: " + fileType);
        }
    }

    /**
     * 설정된 정책의 개수를 반환합니다.
     *
     * @return 정책 개수 (1-5)
     */
    public int size() {
        int count = 0;
        if (imagePolicy != null) count++;
        if (videoPolicy != null) count++;
        if (htmlPolicy != null) count++;
        if (excelPolicy != null) count++;
        if (pdfPolicy != null) count++;
        return count;
    }

    /**
     * Getter Methods - Immutability 보장
     *
     * 주의: nullable 필드이므로 호출 전 hasPolicyFor()로 확인 권장
     */
    public ImagePolicy getImagePolicy() {
        return imagePolicy;
    }

    public VideoPolicy getVideoPolicy() {
        return videoPolicy;
    }

    public HtmlPolicy getHtmlPolicy() {
        return htmlPolicy;
    }

    public ExcelPolicy getExcelPolicy() {
        return excelPolicy;
    }

    public PdfPolicy getPdfPolicy() {
        return pdfPolicy;
    }

    /**
     * Self-Validation: 최소 1개 이상의 정책 필수
     */
    private void validateAtLeastOnePolicy(
        ImagePolicy imagePolicy,
        VideoPolicy videoPolicy,
        HtmlPolicy htmlPolicy,
        ExcelPolicy excelPolicy,
        PdfPolicy pdfPolicy
    ) {
        if (imagePolicy == null && videoPolicy == null && htmlPolicy == null &&
            excelPolicy == null && pdfPolicy == null) {
            throw new IllegalArgumentException(
                "At least one policy must be provided. All policies are null."
            );
        }
    }

    /**
     * FileType null 검증
     */
    private void validateFileType(FileType fileType) {
        if (fileType == null) {
            throw new IllegalArgumentException("FileType must not be null");
        }
    }

    /**
     * Value Object Equality - 모든 정책의 동등성 비교
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileTypePolicies that = (FileTypePolicies) o;
        return Objects.equals(imagePolicy, that.imagePolicy) &&
               Objects.equals(videoPolicy, that.videoPolicy) &&
               Objects.equals(htmlPolicy, that.htmlPolicy) &&
               Objects.equals(excelPolicy, that.excelPolicy) &&
               Objects.equals(pdfPolicy, that.pdfPolicy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(imagePolicy, videoPolicy, htmlPolicy, excelPolicy, pdfPolicy);
    }

    @Override
    public String toString() {
        return String.format(
            "FileTypePolicies{imagePolicy=%s, videoPolicy=%s, htmlPolicy=%s, excelPolicy=%s, pdfPolicy=%s}",
            imagePolicy != null ? "present" : "null",
            videoPolicy != null ? "present" : "null",
            htmlPolicy != null ? "present" : "null",
            excelPolicy != null ? "present" : "null",
            pdfPolicy != null ? "present" : "null"
        );
    }
}
