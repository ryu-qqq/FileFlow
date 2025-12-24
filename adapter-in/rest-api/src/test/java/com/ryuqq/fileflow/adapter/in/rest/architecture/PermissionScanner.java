package com.ryuqq.fileflow.adapter.in.rest.architecture;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.access.prepost.PreAuthorize;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;

/**
 * Permission Scanner - CI/CD용 권한 스캔 도구
 *
 * <p>@PreAuthorize 어노테이션에서 권한 문자열을 추출하여 JSON으로 출력합니다.
 *
 * <p><strong>사용법:</strong>
 *
 * <pre>{@code
 * ./gradlew :adapter-in:rest-api:scanPermissions
 * }</pre>
 *
 * <p><strong>출력 형식:</strong>
 *
 * <pre>{@code
 * {
 *   "serviceName": "authhub",
 *   "scanTimestamp": "2024-01-15T10:30:00",
 *   "permissions": [
 *     {
 *       "key": "user:read",
 *       "locations": ["UserQueryController.getUser()"]
 *     }
 *   ]
 * }
 * }</pre>
 *
 * @author development-team
 * @since 1.0.0
 */
public class PermissionScanner {

    private static final Pattern PERMISSION_PATTERN =
            Pattern.compile(
                    "@access\\.hasPermission\\('([^']+)'\\)|"
                            + "hasAuthority\\('([^']+)'\\)|"
                            + "hasPermission\\([^,]+,\\s*'([^']+)'\\)");

    private static final List<String> EXCLUDED_EXPRESSIONS =
            List.of(
                    "isAuthenticated()",
                    "permitAll()",
                    "denyAll()",
                    "@access.user(",
                    "@access.tenant(",
                    "@access.organization(");

    public static void main(String[] args) {
        String outputPath = "build/permissions/permissions.json";
        String serviceName = "unknown-service";
        String packageName = "com.ryuqq";

        // Parse arguments
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--output":
                    if (i + 1 < args.length) {
                        outputPath = args[++i];
                    }
                    break;
                case "--service":
                    if (i + 1 < args.length) {
                        serviceName = args[++i];
                    }
                    break;
                case "--package":
                    if (i + 1 < args.length) {
                        packageName = args[++i];
                    }
                    break;
                default:
                    // ignore unknown args
                    break;
            }
        }

        try {
            PermissionScanner scanner = new PermissionScanner();
            scanner.scanAndExport(packageName, serviceName, outputPath);
            System.out.println("Permission scan completed: " + outputPath);
        } catch (Exception e) {
            System.err.println("Permission scan failed: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * 패키지 스캔 및 JSON 출력
     *
     * @param packageName 스캔할 패키지
     * @param serviceName 서비스 이름
     * @param outputPath JSON 출력 경로
     */
    public void scanAndExport(String packageName, String serviceName, String outputPath)
            throws IOException {
        JavaClasses classes =
                new ClassFileImporter()
                        .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                        .importPackages(packageName);

        Map<String, List<String>> permissionLocations = new LinkedHashMap<>();

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
                String location = javaClass.getSimpleName() + "." + method.getName() + "()";

                for (String permission : permissions) {
                    permissionLocations
                            .computeIfAbsent(permission, k -> new ArrayList<>())
                            .add(location);
                }
            }
        }

        // Build output structure
        Map<String, Object> output = new LinkedHashMap<>();
        output.put("serviceName", serviceName);
        output.put(
                "scanTimestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        output.put("totalCount", permissionLocations.size());

        List<Map<String, Object>> permissionList = new ArrayList<>();
        Set<String> sortedKeys = new TreeSet<>(permissionLocations.keySet());

        for (String key : sortedKeys) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("key", key);
            item.put("locations", permissionLocations.get(key));
            permissionList.add(item);
        }

        output.put("permissions", permissionList);

        // Write JSON
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        File outputFile = new File(outputPath);
        outputFile.getParentFile().mkdirs();
        mapper.writeValue(outputFile, output);

        // Print summary
        System.out.println("\n╔══════════════════════════════════════════════════════════╗");
        System.out.println("║           PERMISSION SCAN RESULT                          ║");
        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║  Service: %-46s ║%n", serviceName);
        System.out.printf("║  Package: %-46s ║%n", truncate(packageName, 46));
        System.out.printf("║  Permissions Found: %-36d ║%n", permissionLocations.size());
        System.out.println("╠══════════════════════════════════════════════════════════╣");

        for (String key : sortedKeys) {
            System.out.printf("║  %-56s ║%n", key);
        }

        System.out.println("╠══════════════════════════════════════════════════════════╣");
        System.out.printf("║  Output: %-47s ║%n", truncate(outputPath, 47));
        System.out.println("╚══════════════════════════════════════════════════════════╝\n");
    }

    private boolean isExcludedExpression(String expression) {
        return EXCLUDED_EXPRESSIONS.stream().anyMatch(expression::contains);
    }

    private List<String> extractPermissions(String expression) {
        List<String> permissions = new ArrayList<>();
        Matcher matcher = PERMISSION_PATTERN.matcher(expression);

        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                String match = matcher.group(i);
                if (match != null && !match.isBlank()) {
                    permissions.add(match);
                }
            }
        }

        return permissions;
    }

    private String truncate(String str, int maxLength) {
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }
}
