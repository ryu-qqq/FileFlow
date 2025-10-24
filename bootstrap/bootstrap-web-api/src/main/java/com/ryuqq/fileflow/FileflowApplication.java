package com.ryuqq.fileflow;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * FileFlow - Tenant-based File Upload and Processing Pipeline Platform
 *
 * <p>Main Spring Boot application entry point for FileFlow REST API.
 * This application provides a multi-tenant file upload and processing pipeline
 * with tenant and organization management capabilities.</p>
 *
 * @author FileFlow Team
 * @since 2025-10-04
 */
@SpringBootApplication
@OpenAPIDefinition(
    info = @Info(
        title = "FileFlow API",
        version = "1.0.0",
        description = "Multi-tenant File Upload and Processing Pipeline REST API",
        contact = @Contact(
            name = "FileFlow Team",
            email = "team@fileflow.com"
        )
    ),
    servers = {
        @Server(
            url = "http://localhost:8080",
            description = "Local Development Server"
        )
    }
)
public class FileflowApplication {

    public static void main(String[] args) {
        SpringApplication.run(FileflowApplication.class, args);
    }
}
