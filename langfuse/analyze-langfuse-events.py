#!/usr/bin/env python3
"""
LangFuse Events 자동 분석 스크립트

목적: LangFuse에서 Hook 실행 이벤트를 조회하여 위반 패턴 분석

주요 기능:
1. LangFuse Events 조회 (hook-execution-*)
2. 위반 패턴 분석:
   - Layer별 위반 건수
   - 규칙별 위반 빈도
   - 시간대별 트렌드
3. Serena & Skills 사용 패턴 분석
4. 개선 제안 생성

사용법:
    # 최근 7일 데이터 분석
    python3 scripts/langfuse/analyze-langfuse-events.py

    # 특정 기간 분석
    python3 scripts/langfuse/analyze-langfuse-events.py --days 30

    # 보고서 파일 생성
    python3 scripts/langfuse/analyze-langfuse-events.py --output report.md
"""

import os
import sys
import json
from pathlib import Path
from datetime import datetime, timedelta
from typing import Dict, List, Optional
from collections import defaultdict, Counter
from langfuse import Langfuse
from dotenv import load_dotenv

# 프로젝트 루트
PROJECT_ROOT = Path(__file__).parent.parent.parent
load_dotenv(PROJECT_ROOT / ".env")


class LangFuseAnalyzer:
    """LangFuse Events 분석기"""

    def __init__(self):
        """LangFuse 클라이언트 초기화"""
        public_key = os.getenv('LANGFUSE_PUBLIC_KEY')
        secret_key = os.getenv('LANGFUSE_SECRET_KEY')
        host = os.getenv('LANGFUSE_HOST', 'https://us.cloud.langfuse.com')

        if not public_key or not secret_key:
            raise ValueError("LANGFUSE_PUBLIC_KEY 및 LANGFUSE_SECRET_KEY 환경 변수가 필요합니다")

        self.langfuse = Langfuse(
            public_key=public_key,
            secret_key=secret_key,
            host=host
        )
        print(f"✅ LangFuse 클라이언트 초기화 완료 (Host: {host})")

    def fetch_hook_events(self, days: int = 7) -> List[Dict]:
        """
        LangFuse에서 Hook 실행 이벤트 조회

        Args:
            days: 조회 기간 (일수)

        Returns:
            Hook 이벤트 리스트
        """
        print(f"📊 최근 {days}일간의 Hook 이벤트 조회 중...")

        # 시작 날짜 계산
        start_date = datetime.now() - timedelta(days=days)

        # LangFuse Events 조회
        # Note: LangFuse SDK v2.x에서는 client.get_events() 메서드가 없을 수 있음
        # 대신 LangFuse API를 직접 호출해야 할 수 있음
        # 여기서는 placeholder로 작성

        events = []

        # TODO: LangFuse API 직접 호출로 구현
        # GET https://us.cloud.langfuse.com/api/public/events
        # Filter: name startswith "hook-execution-"
        # Filter: timestamp >= start_date

        print(f"⚠️ 주의: LangFuse SDK v2.x에서는 Events 조회 API가 제공되지 않을 수 있습니다.")
        print(f"   대신 LangFuse Dashboard에서 수동으로 조회하거나,")
        print(f"   REST API를 직접 호출해야 합니다.")

        return events

    def analyze_violations(self, events: List[Dict]) -> Dict:
        """
        위반 패턴 분석

        Args:
            events: Hook 이벤트 리스트

        Returns:
            분석 결과 딕셔너리
        """
        print("🔍 위반 패턴 분석 중...")

        analysis = {
            "total_sessions": len(events),
            "total_violations": 0,
            "violations_by_layer": Counter(),
            "zero_tolerance_compliance_rate": 0.0,
            "cache_injection_success_rate": 0.0,
            "serena_usage_stats": {
                "total_memory_loads": 0,
                "total_tool_usage": 0,
                "avg_tokens_per_load": 0
            },
            "skills_usage_stats": {
                "total_skills_detected": 0,
                "skills_frequency": Counter()
            }
        }

        total_violations_count = 0
        sessions_with_violations = 0
        cache_successes = 0

        for event in events:
            output = event.get('output', {})
            metadata = event.get('metadata', {})

            # 위반 건수 집계
            violations = output.get('total_violations', 0)
            total_violations_count += violations

            if violations > 0:
                sessions_with_violations += 1

            # Layer별 위반 (metadata에서 추출)
            layers_injected = output.get('layers_injected', 0)
            if violations > 0 and layers_injected > 0:
                # 간접적으로 추론: 어떤 layer에서 위반이 발생했는지
                # (실제 데이터에는 layer별 세분화가 필요)
                analysis["violations_by_layer"]["unknown"] += violations

            # Cache 주입 성공률
            if output.get('cache_injection_success', False):
                cache_successes += 1

            # Serena 사용 통계
            if output.get('serena_memory_loaded', False):
                analysis["serena_usage_stats"]["total_memory_loads"] += 1
                analysis["serena_usage_stats"]["total_tool_usage"] += output.get('serena_tool_usage_count', 0)
                tokens = output.get('serena_total_memory_tokens', 0)
                if tokens > 0:
                    analysis["serena_usage_stats"]["avg_tokens_per_load"] += tokens

            # Skills 사용 통계
            skills_count = output.get('skills_detected_count', 0)
            if skills_count > 0:
                analysis["skills_usage_stats"]["total_skills_detected"] += skills_count
                skills_used = output.get('skills_used', [])
                for skill in skills_used:
                    analysis["skills_usage_stats"]["skills_frequency"][skill] += 1

        # 평균 계산
        if len(events) > 0:
            analysis["total_violations"] = total_violations_count
            analysis["zero_tolerance_compliance_rate"] = (
                (len(events) - sessions_with_violations) / len(events) * 100
            )
            analysis["cache_injection_success_rate"] = (
                cache_successes / len(events) * 100
            )

            if analysis["serena_usage_stats"]["total_memory_loads"] > 0:
                analysis["serena_usage_stats"]["avg_tokens_per_load"] = (
                    analysis["serena_usage_stats"]["avg_tokens_per_load"] /
                    analysis["serena_usage_stats"]["total_memory_loads"]
                )

        return analysis

    def generate_report(self, analysis: Dict, output_file: Optional[str] = None):
        """
        분석 보고서 생성

        Args:
            analysis: 분석 결과
            output_file: 출력 파일 경로 (None이면 stdout)
        """
        print("📝 보고서 생성 중...")

        report_lines = [
            "# LangFuse Hook 시스템 분석 보고서",
            "",
            f"**생성 일시**: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}",
            "",
            "---",
            "",
            "## 📊 전체 통계",
            "",
            f"- **총 세션 수**: {analysis['total_sessions']}",
            f"- **총 위반 건수**: {analysis['total_violations']}",
            f"- **Zero-Tolerance 준수율**: {analysis['zero_tolerance_compliance_rate']:.1f}%",
            f"- **Cache 주입 성공률**: {analysis['cache_injection_success_rate']:.1f}%",
            "",
            "---",
            "",
            "## 🚨 위반 패턴 분석",
            "",
        ]

        # Layer별 위반
        if analysis['violations_by_layer']:
            report_lines.append("### Layer별 위반 건수")
            report_lines.append("")
            for layer, count in analysis['violations_by_layer'].most_common():
                report_lines.append(f"- **{layer}**: {count}회")
            report_lines.append("")
        else:
            report_lines.append("✅ 위반 사항 없음")
            report_lines.append("")

        # Serena 사용 통계
        report_lines.extend([
            "---",
            "",
            "## 🧠 Serena Memory 사용 통계",
            "",
            f"- **Memory 로드 횟수**: {analysis['serena_usage_stats']['total_memory_loads']}",
            f"- **Serena 도구 사용 횟수**: {analysis['serena_usage_stats']['total_tool_usage']}",
            f"- **평균 토큰 수/로드**: {analysis['serena_usage_stats']['avg_tokens_per_load']:.0f}",
            ""
        ])

        # Skills 사용 통계
        report_lines.extend([
            "---",
            "",
            "## 🎯 Skills 사용 통계",
            "",
            f"- **총 Skills 감지 횟수**: {analysis['skills_usage_stats']['total_skills_detected']}",
            ""
        ])

        if analysis['skills_usage_stats']['skills_frequency']:
            report_lines.append("### Skill별 사용 빈도")
            report_lines.append("")
            for skill, count in analysis['skills_usage_stats']['skills_frequency'].most_common():
                report_lines.append(f"- **{skill}**: {count}회")
            report_lines.append("")

        # 개선 제안
        report_lines.extend([
            "---",
            "",
            "## 💡 개선 제안",
            "",
        ])

        # Zero-Tolerance 준수율 기반 제안
        compliance_rate = analysis['zero_tolerance_compliance_rate']
        if compliance_rate < 80:
            report_lines.append(f"⚠️ **Zero-Tolerance 준수율이 {compliance_rate:.1f}%로 낮습니다.**")
            report_lines.append("   - 프롬프트 강화 필요")
            report_lines.append("   - 위반 규칙 재교육")
            report_lines.append("")
        elif compliance_rate < 95:
            report_lines.append(f"⚠️ **Zero-Tolerance 준수율이 {compliance_rate:.1f}%입니다.**")
            report_lines.append("   - 추가 개선 여지 있음")
            report_lines.append("")
        else:
            report_lines.append(f"✅ **Zero-Tolerance 준수율이 {compliance_rate:.1f}%로 우수합니다.**")
            report_lines.append("")

        # Cache 주입 성공률 기반 제안
        cache_rate = analysis['cache_injection_success_rate']
        if cache_rate < 80:
            report_lines.append(f"⚠️ **Cache 주입 성공률이 {cache_rate:.1f}%로 낮습니다.**")
            report_lines.append("   - 키워드 매핑 검토 필요")
            report_lines.append("   - Layer 감지 로직 개선")
            report_lines.append("")

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

    parser = argparse.ArgumentParser(description="LangFuse Events 자동 분석")
    parser.add_argument('--days', type=int, default=7, help="조회 기간 (일수)")
    parser.add_argument('--output', type=str, help="보고서 출력 파일 (Markdown)")

    args = parser.parse_args()

    try:
        analyzer = LangFuseAnalyzer()

        # Events 조회
        events = analyzer.fetch_hook_events(days=args.days)

        if not events:
            print("⚠️ 조회된 이벤트가 없습니다.")
            print("   LangFuse Dashboard에서 수동으로 확인하세요:")
            print("   https://us.cloud.langfuse.com/project/claude-spring-standards")
            return

        # 분석
        analysis = analyzer.analyze_violations(events)

        # 보고서 생성
        analyzer.generate_report(analysis, output_file=args.output)

    except Exception as e:
        print(f"❌ 오류 발생: {e}")
        sys.exit(1)


if __name__ == "__main__":
    main()
