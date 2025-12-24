package com.ryuqq.fileflow.adapter.in.rest.architecture;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.prepost.PreAuthorize;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

import static com.ryuqq.fileflow.adapter.in.rest.architecture.ArchUnitPackageConstants.ADAPTER_IN_REST;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Permission 네이밍 컨벤션 검증 테스트
 *
 * <p>@PreAuthorize 어노테이션에 사용된 권한 문자열의 네이밍 규칙을 검증합니다.
 *
 * <p><strong>네이밍 컨벤션:</strong>
 *
 * <ul>
 *   <li>형식: {service}:{domain}:{action} 또는 {domain}:{action}
 *   <li>소문자 + 하이픈만 허용 (a-z, 0-9, -)
 *   <li>예시: authhub:user:read, product-service:product:write, user:read
 * </ul>
 *
 * <p><strong>검증 대상:</strong>
 *
 * <ul>
 *   <li>@PreAuthorize("@access.hasPermission('xxx')") 패턴
 *   <li>@PreAuthorize("hasAuthority('xxx')") 패턴
 *   <li>@PreAuthorize("hasPermission(..., 'xxx')") 패턴
 * </ul>
 *
 * <p><strong>예외 (검증 제외):</strong>
 *
 * <ul>
 *   <li>isAuthenticated()
 *   <li>permitAll()
 *   <li>denyAll()
 *   <li>@access.user(...) - 리소스 기반 접근 제어
 *   <li>@access.tenant(...) - 테넌트 기반 접근 제어
 * </ul>
 *
 * @author development-team
 * @since 1.0.0
 */
@DisplayName("Permission Naming Convention Tests")
@Tag("architecture")
@Tag("permission")
class PermissionNamingArchTest {

    private static JavaClasses classes;

    /**
     * 권한 문자열 추출 패턴
     *
     * <p>다음 패턴에서 권한 문자열을 추출합니다:
     *
     * <ul>
     *   <li>@access.hasPermission('user:read')
     *   <li>hasAuthority('ROLE_ADMIN')
     *   <li>hasPermission(#obj, 'read')
     * </ul>
     */
    private static final Pattern PERMISSION_PATTERN =
            Pattern.compile(
                    "@access\\.hasPermission\\('([^']+)'\\)|"
                            + "hasAuthority\\('([^']+)'\\)|"
                            + "hasPermission\\([^,]+,\\s*'([^']+)'\\)");

    /**
     * 유효한 권한 형식 패턴
     *
     * <p>허용 형식:
     *
     * <ul>
     *   <li>{domain}:{action} - 예: user:read
     *   <li>{service}:{domain}:{action} - 예: authhub:user:read
     * </ul>
     *
     * <p>각 세그먼트는 소문자, 숫자, 하이픈만 허용 (1자 이상)
     */
    private static final Pattern VALID_PERMISSION_FORMAT =
            Pattern.compile("^[a-z][a-z0-9-]*:[a-z][a-z0-9-]*(:[a-z][a-z0-9-]*)?$");

    /** 검증에서 제외할 SpEL 표현식 패턴 */
    private static final List<String> EXCLUDED_EXPRESSIONS =
            List.of(
                    "isAuthenticated()",
                    "permitAll()",
                    "denyAll()",
                    "@access.user(", // 리소스 기반 접근 제어
                    "@access.tenant(", // 테넌트 기반 접근 제어
                    "@access.organization(" // 조직 기반 접근 제어
                    );

    @BeforeAll
    static void setUp() {
        classes =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(ADAPTER_IN_REST);
    }

    @Test
    @DisplayName("[필수] @PreAuthorize 권한 문자열은 네이밍 컨벤션을 준수해야 한다")
    void preAuthorize_MustFollowNamingConvention() {
        List<String> violations = new ArrayList<>();
        Set<String> validPermissions = new HashSet<>();

        for (JavaClass javaClass : classes) {
            for (JavaMethod method : javaClass.getMethods()) {
                if (!method.isAnnotatedWith(PreAuthorize.class)) {
                    continue;
                }

                PreAuthorize annotation = method.getAnnotationOfType(PreAuthorize.class);
                String expression = annotation.value();

                // 제외 대상 체크
                if (isExcludedExpression(expression)) {
                    continue;
                }

                // 권한 문자열 추출
                List<String> permissions = extractPermissions(expression);

                for (String permission : permissions) {
                    if (!isValidPermissionFormat(permission)) {
                        violations.add(
                                String.format(
                                        "Invalid permission format: '%s' in %s.%s() - Expected"
                                                + " format: {domain}:{action} or"
                                                + " {service}:{domain}:{action}",
                                        permission, javaClass.getSimpleName(), method.getName()));
                    } else {
                        validPermissions.add(permission);
                    }
                }
            }
        }

        // 발견된 유효 권한 출력 (CI/CD 스캔용 로그)
        if (!validPermissions.isEmpty()) {
            System.out.println("\n=== Discovered Permissions ===");
            validPermissions.stream().sorted().forEach(p -> System.out.println("  - " + p));
            System.out.println("==============================\n");
        }

        assertThat(violations).as("Permission naming violations found").isEmpty();
    }

    @Test
    @DisplayName("[정보] 프로젝트 내 모든 권한 목록 출력")
    void listAllPermissions() {
        Set<String> allPermissions = new HashSet<>();

        for (JavaClass javaClass : classes) {
            for (JavaMethod method : javaClass.getMethods()) {
                if (!method.isAnnotatedWith(PreAuthorize.class)) {
                    continue;
                }

                PreAuthorize annotation = method.getAnnotationOfType(PreAuthorize.class);
                String expression = annotation.value();

                if (isExcludedExpression(expression)) {
                    continue;
                }

                List<String> permissions = extractPermissions(expression);
                allPermissions.addAll(permissions);
            }
        }

        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║          PROJECT PERMISSION INVENTORY                     ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        if (allPermissions.isEmpty()) {
            System.out.println("║  No permissions found                                     ║");
        } else {
            allPermissions.stream().sorted().forEach(p -> System.out.printf("║  %-56s ║%n", p));
        }

        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║  Total: %-48d ║%n", allPermissions.size());
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");

        // 항상 성공 (정보 출력용)
        assertThat(true).isTrue();
    }

    /**
     * 제외 대상 표현식인지 확인
     *
     * @param expression SpEL 표현식
     * @return 제외 대상이면 true
     */
    private boolean isExcludedExpression(String expression) {
        return EXCLUDED_EXPRESSIONS.stream().anyMatch(expression::contains);
    }

    /**
     * SpEL 표현식에서 권한 문자열 추출
     *
     * @param expression SpEL 표현식
     * @return 추출된 권한 문자열 목록
     */
    private List<String> extractPermissions(String expression) {
        List<String> permissions = new ArrayList<>();
        Matcher matcher = PERMISSION_PATTERN.matcher(expression);

        while (matcher.find()) {
            // 3개 그룹 중 매칭된 것 찾기
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String match = matcher.group(i);
                if (match != null && !match.isBlank()) {
                    permissions.add(match);
                }
            }
        }

        return permissions;
    }

    /**
     * 권한 형식이 유효한지 확인
     *
     * @param permission 권한 문자열
     * @return 유효하면 true
     */
    private boolean isValidPermissionFormat(String permission) {
        // ROLE_ 접두사는 Spring Security 역할로 별도 처리
        if (permission.startsWith("ROLE_")) {
            return true;
        }

        return VALID_PERMISSION_FORMAT.matcher(permission).matches();
    }
}
