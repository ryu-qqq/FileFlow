#!/bin/bash

# ============================================================================
# Multi-Bootstrap Docker Build Test Script
# ============================================================================
# 4개 Bootstrap을 모두 빌드하여 Dockerfile이 올바르게 작동하는지 검증
# ============================================================================

set -e

echo "========================================="
echo "Multi-Bootstrap Docker Build Test"
echo "========================================="
echo ""

# Bootstrap 정의
declare -A BOOTSTRAPS=(
    ["web-api"]="8080:/actuator/health"
    ["scheduler-download"]="9091:/actuator/health"
    ["scheduler-pipeline"]="9092:/actuator/health"
    ["scheduler-upload"]="9093:/actuator/health"
)

SUCCESS_COUNT=0
FAIL_COUNT=0

# 각 Bootstrap 빌드
for BOOTSTRAP in "${!BOOTSTRAPS[@]}"; do
    IFS=':' read -r PORT HEALTH_PATH <<< "${BOOTSTRAPS[$BOOTSTRAP]}"

    echo "========================================="
    echo "Building: $BOOTSTRAP"
    echo "Port: $PORT"
    echo "Health Path: $HEALTH_PATH"
    echo "========================================="

    IMAGE_NAME="fileflow:${BOOTSTRAP}-test"

    # Docker 빌드
    if docker build \
        --build-arg BOOTSTRAP_NAME="$BOOTSTRAP" \
        --build-arg EXPOSE_PORT="$PORT" \
        --build-arg HEALTH_CHECK_PATH="$HEALTH_PATH" \
        -t "$IMAGE_NAME" \
        -f Dockerfile \
        . ; then

        echo "✅ Build successful: $BOOTSTRAP"
        SUCCESS_COUNT=$((SUCCESS_COUNT + 1))

        # 이미지 정보 출력
        echo ""
        echo "Image Info:"
        docker images "$IMAGE_NAME" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}"
        echo ""

    else
        echo "❌ Build failed: $BOOTSTRAP"
        FAIL_COUNT=$((FAIL_COUNT + 1))
    fi

    echo ""
done

echo "========================================="
echo "Build Summary"
echo "========================================="
echo "✅ Success: $SUCCESS_COUNT"
echo "❌ Failed: $FAIL_COUNT"
echo "Total: ${#BOOTSTRAPS[@]}"
echo ""

if [ $FAIL_COUNT -eq 0 ]; then
    echo "🎉 All bootstraps built successfully!"

    echo ""
    echo "========================================="
    echo "All Docker Images"
    echo "========================================="
    docker images "fileflow:*-test" --format "table {{.Repository}}\t{{.Tag}}\t{{.Size}}\t{{.CreatedAt}}"

    exit 0
else
    echo "⚠️  Some bootstraps failed to build."
    exit 1
fi
