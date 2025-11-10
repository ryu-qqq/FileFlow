#!/usr/bin/env python3
"""
프롬프트 자동 개선 제안 시스템

목적: 위반 패턴 분석 결과를 기반으로 Layer별 프롬프트 개선 제안 생성

주요 기능:
1. 위반 패턴 입력 (analyze-langfuse-events.py 결과)
2. Layer별 프롬프트 개선 제안:
   - 위반이 많은 규칙 강조
   - 예시 코드 추가
   - 금지 패턴 명확화
3. LangFuse Prompt 버전 생성 (v1.0 → v1.1)
4. A/B 테스트 설정 안내

사용법:
    # 분석 결과 기반 제안 생성
    python3 scripts/langfuse/suggest-prompt-improvements.py

    # 특정 Layer 프롬프트 개선
    python3 scripts/langfuse/suggest-prompt-improvements.py --layer domain

    # 제안 내용을 파일로 저장
    python3 scripts/langfuse/suggest-prompt-improvements.py --output improvements.md
"""

import os
import sys
import json
from pathlib import Path
from datetime import datetime
from typing import Dict, List, Optional
from collections import Counter

# 프로젝트 루트
PROJECT_ROOT = Path(__file__).parent.parent.parent


class PromptImprovementSuggester:
    """프롬프트 개선 제안 생성기"""

    def __init__(self):
        """초기화"""
        self.cache_dir = PROJECT_ROOT / ".claude" / "cache" / "rules"
        self.index_file = self.cache_dir / "index.json"
        print("✅ 프롬프트 개선 제안 생성기 초기화")

    def load_violation_patterns(self) -> Dict:
        """
        위반 패턴 로드 (실제로는 analyze-langfuse-events.py 결과 사용)

        Returns:
            위반 패턴 딕셔너리
        """
        # Placeholder: 실제로는 LangFuse에서 가져온 데이터 사용
        return {
            "violations_by_layer": {
                "domain": 15,
                "application": 8,
                "adapter-rest": 5,
                "adapter-persistence": 3
            },
            "violations_by_rule": {
                "lombok-prohibition": 10,
                "law-of-demeter": 8,
                "javadoc-missing": 7,
                "transaction-boundary": 5
            },
            "total_violations": 31
        }

    def load_layer_rules(self, layer: str) -> List[Dict]:
        """
        Layer별 규칙 로드 (Cache에서)

        Args:
            layer: domain, application 등

        Returns:
            규칙 리스트
        """
        if not self.index_file.exists():
            print(f"⚠️ Cache index 파일이 없습니다: {self.index_file}")
            return []

        with open(self.index_file, 'r', encoding='utf-8') as f:
            index = json.load(f)

        rule_ids = index.get("layerIndex", {}).get(layer, [])
        rules = []

        for rule_id in rule_ids:
            rule_file = self.cache_dir / f"{rule_id}.json"
            if rule_file.exists():
                with open(rule_file, 'r', encoding='utf-8') as f:
                    rules.append(json.load(f))

        return rules

    def generate_improvement_suggestions(
        self,
        layer: Optional[str] = None
    ) -> Dict[str, str]:
        """
        프롬프트 개선 제안 생성

        Args:
            layer: 특정 Layer (None이면 전체)

        Returns:
            Layer별 개선 제안 딕셔너리
        """
        print("🔍 프롬프트 개선 제안 생성 중...")

        violations = self.load_violation_patterns()
        suggestions = {}

        layers_to_process = [layer] if layer else ["domain", "application", "adapter-rest", "adapter-persistence"]

        for target_layer in layers_to_process:
            violation_count = violations["violations_by_layer"].get(target_layer, 0)

            if violation_count == 0:
                suggestions[target_layer] = f"✅ **{target_layer}**: 위반 없음. 현재 프롬프트 유지."
                continue

            # 규칙 로드
            rules = self.load_layer_rules(target_layer)

            # 개선 제안 생성
            improvement_lines = [
                f"## 🎯 {target_layer.upper()} Layer 프롬프트 개선 제안",
                "",
                f"**현재 위반 건수**: {violation_count}회",
                "",
                "### 📋 제안 사항",
                ""
            ]

            # 1. 위반이 많은 규칙 강조
            improvement_lines.extend([
                "#### 1. 위반 빈도가 높은 규칙 강조",
                "",
                "프롬프트 상단에 **굵은 글씨**와 **🚨 아이콘**으로 다음 규칙 강조:",
                ""
            ])

            critical_rules = [r for r in rules if r.get("metadata", {}).get("priority") == "critical"]
            for rule in critical_rules[:3]:  # 상위 3개만
                rule_title = rule.get("metadata", {}).get("title", "Unknown")
                improvement_lines.append(f"- 🚨 **{rule_title}**")

            improvement_lines.append("")

            # 2. 예시 코드 추가
            improvement_lines.extend([
                "#### 2. 예시 코드 추가",
                "",
                "위반 사례와 올바른 사례를 명확히 대조:",
                "",
                "```java",
                "// ❌ 잘못된 예시 (Lombok 사용)",
                "@Data",
                "public class Order {",
                "    private Long id;",
                "}",
                "",
                "// ✅ 올바른 예시 (Pure Java)",
                "public class Order {",
                "    private Long id;",
                "",
                "    public Long getId() {",
                "        return id;",
                "    }",
                "",
                "    public void setId(Long id) {",
                "        this.id = id;",
                "    }",
                "}",
                "```",
                ""
            ])

            # 3. 금지 패턴 명확화
            improvement_lines.extend([
                "#### 3. 금지 패턴 명확화",
                "",
                "정규식 패턴으로 명확히 정의:",
                ""
            ])

            for rule in critical_rules[:2]:
                prohibited = rule.get("rules", {}).get("prohibited", [])
                if prohibited:
                    improvement_lines.append(f"- **금지**: {prohibited[0]}")

            improvement_lines.append("")

            # 4. 검증 강화
            improvement_lines.extend([
                "#### 4. 자동 검증 강화",
                "",
                f"- `validation-helper.py`에 {target_layer} Layer 전용 검증 규칙 추가",
                "- Git pre-commit hook 검증 강화",
                "- ArchUnit 테스트 규칙 추가",
                ""
            ])

            # 5. LangFuse Prompt 버전 업그레이드
            improvement_lines.extend([
                "---",
                "",
                "### 🚀 LangFuse Prompt 버전 업그레이드",
                "",
                f"**현재 버전**: v1.0",
                f"**제안 버전**: v1.1",
                "",
                "**변경 사항**:",
                f"- {target_layer} Layer 규칙 강조 강화",
                "- 예시 코드 3개 추가",
                "- 금지 패턴 정규식 명확화",
                "",
                "**업그레이드 명령어**:",
                "```bash",
                f"python3 .claude/commands/lib/register_prompt_to_langfuse.py \\",
                f"    --layer {target_layer} \\",
                "    --version v1.1 \\",
                "    --prompt-file langfuse/prompts/{target_layer}-layer-v1.1.md",
                "```",
                ""
            ])

            suggestions[target_layer] = "\n".join(improvement_lines)

        return suggestions

    def generate_ab_test_guide(self) -> str:
        """
        A/B 테스트 설정 가이드 생성

        Returns:
            A/B 테스트 가이드 (Markdown)
        """
        guide_lines = [
            "## 🧪 A/B 테스트 설정 가이드",
            "",
            "### 목적",
            "프롬프트 v1.0과 v1.1의 효과를 비교하여 개선 여부 검증",
            "",
            "### 실험 설계",
            "",
            "| 그룹 | 프롬프트 버전 | 기간 | 측정 메트릭 |",
            "|------|--------------|------|-------------|",
            "| A (Control) | v1.0 | 1주 | 위반 건수, 토큰 사용량 |",
            "| B (Treatment) | v1.1 | 1주 | 위반 건수, 토큰 사용량 |",
            "",
            "### 성공 기준",
            "",
            "- ✅ **위반 건수**: 30% 이상 감소",
            "- ✅ **Zero-Tolerance 준수율**: 95% 이상",
            "- ✅ **토큰 사용량**: 10% 이내 증가 허용",
            "",
            "### 실행 단계",
            "",
            "1. **v1.1 프롬프트 등록**:",
            "   ```bash",
            "   python3 .claude/commands/lib/register_prompt_to_langfuse.py --layer domain --version v1.1",
            "   ```",
            "",
            "2. **LangFuse에서 A/B 테스트 설정**:",
            "   - Dashboard → Prompts → domain-layer",
            "   - Version v1.0: 50% traffic",
            "   - Version v1.1: 50% traffic",
            "",
            "3. **1주일 운영 후 결과 분석**:",
            "   ```bash",
            "   python3 scripts/langfuse/analyze-langfuse-events.py --days 7 --output week1-results.md",
            "   ```",
            "",
            "4. **승자 결정**:",
            "   - v1.1이 성공 기준 충족 시 → 100% traffic",
            "   - 실패 시 → v1.0 유지 또는 v1.2 시도",
            "",
            "---",
            ""
        ]

        return "\n".join(guide_lines)

    def generate_full_report(
        self,
        layer: Optional[str] = None,
        output_file: Optional[str] = None
    ):
        """
        전체 개선 제안 보고서 생성

        Args:
            layer: 특정 Layer (None이면 전체)
            output_file: 출력 파일 경로
        """
        print("📝 개선 제안 보고서 생성 중...")

        report_lines = [
            "# 프롬프트 자동 개선 제안 보고서",
            "",
            f"**생성 일시**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "---",
            ""
        ]

        # Layer별 개선 제안
        suggestions = self.generate_improvement_suggestions(layer=layer)

        for layer_name, suggestion in suggestions.items():
            report_lines.append(suggestion)
            report_lines.append("")
            report_lines.append("---")
            report_lines.append("")

        # A/B 테스트 가이드
        ab_test_guide = self.generate_ab_test_guide()
        report_lines.append(ab_test_guide)

        report_text = "\n".join(report_lines)

        # 출력
        if output_file:
            output_path = Path(output_file)
            output_path.parent.mkdir(parents=True, exist_ok=True)
            with open(output_path, 'w', encoding='utf-8') as f:
                f.write(report_text)
            print(f"✅ 보고서 저장: {output_path}")
        else:
            print("\n" + report_text)


def main():
    """메인 함수"""
    import argparse

    parser = argparse.ArgumentParser(description="프롬프트 자동 개선 제안 생성")
    parser.add_argument('--layer', type=str, help="특정 Layer (domain, application, etc.)")
    parser.add_argument('--output', type=str, help="보고서 출력 파일 (Markdown)")

    args = parser.parse_args()

    try:
        suggester = PromptImprovementSuggester()
        suggester.generate_full_report(layer=args.layer, output_file=args.output)

    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
