#!/bin/bash

OLD_PACKAGE="com.company.template"
NEW_PACKAGE="com.ryuqq.fileflow"

OLD_PATH="com/company/template"
NEW_PATH="com/ryuqq/fileflow"

echo "🔄 Renaming packages from $OLD_PACKAGE to $NEW_PACKAGE..."

# 1. Java 파일 내용 변경
echo "📝 Updating Java file contents..."
find . -name "*.java" -type f -exec sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} +

# 2. build.gradle.kts 파일 변경
echo "📝 Updating build.gradle.kts files..."
find . -name "build.gradle.kts" -type f -exec sed -i '' "s/$OLD_PACKAGE/$NEW_PACKAGE/g" {} +

# 3. 디렉토리 구조 변경
echo "📁 Restructuring directories..."
for dir in $(find . -type d -path "*/$OLD_PATH" 2>/dev/null); do
    NEW_DIR=$(echo "$dir" | sed "s|$OLD_PATH|$NEW_PATH|g")
    mkdir -p "$(dirname "$NEW_DIR")"

    # ryuqq 디렉토리까지만 생성
    RYUQQ_DIR=$(echo "$NEW_DIR" | sed 's|/fileflow.*||')
    if [ ! -d "$RYUQQ_DIR" ]; then
        mkdir -p "$RYUQQ_DIR"
    fi

    # fileflow 디렉토리 생성
    FILEFLOW_PARENT=$(dirname "$NEW_DIR")
    if [ ! -d "$FILEFLOW_PARENT" ]; then
        mkdir -p "$FILEFLOW_PARENT"
    fi

    # 이동
    if [ -d "$dir" ] && [ ! -d "$NEW_DIR" ]; then
        mv "$dir" "$NEW_DIR"
        echo "✅ Moved: $dir -> $NEW_DIR"
    fi
done

# 4. 빈 디렉토리 정리
echo "🧹 Cleaning up empty directories..."
find . -type d -path "*/com/company" -empty -delete 2>/dev/null
find . -type d -path "*/com" -empty -delete 2>/dev/null

echo "✨ Package rename complete!"
echo "⚠️  Please verify changes with: git diff"
