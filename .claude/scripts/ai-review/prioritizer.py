#!/usr/bin/env python3
"""
AI Review Prioritizer

투표 시스템 + Zero-Tolerance 규칙 기반 우선순위 계산
- 3봇 합의 → Critical
- 2봇 합의 → Important
- 1봇만 → Suggestion
- Zero-Tolerance 위반 → 자동 Critical
"""

import re
from typing import List, Dict
from dataclasses import dataclass


@dataclass
class PrioritizedIssue:
    """우선순위가 결정된 이슈"""
    id: str
    file: str
    line: int
    category: str
    description: str
    bots: List[str]
    vote_count: int
    priority: str  # Critical, Important, Suggestion
    reason: str    # 우선순위 결정 이유
    effort: str    # S(Small), M(Medium), L(Large)
    zero_tolerance: bool = False  # Zero-Tolerance 규칙 위반 여부


class Prioritizer:
    """우선순위 계산 클래스"""

    # Zero-Tolerance 패턴
    ZERO_TOLERANCE_PATTERNS = {
        "Lombok 사용": {
            "pattern": r"@(Data|Builder|Getter|Setter|AllArgsConstructor|NoArgsConstructor|RequiredArgsConstructor|ToString|EqualsAndHashCode)",
            "reason": "Zero-Tolerance: Lombok 금지 (Pure Java 사용)"
        },
        "Law of Demeter 위반": {
            "pattern": r"\.get\w+\(\)\.get\w+\(\)",
            "reason": "Zero-Tolerance: Getter 체이닝 금지 (Tell, Don't Ask)"
        },
        "Transaction 경계 위반": {
            "pattern": r"@Transactional.*?(RestTemplate|WebClient|HttpClient|FeignClient)",
            "reason": "Zero-Tolerance: @Transactional 내 외부 API 호출 금지"
        },
        "Long FK 위반": {
            "pattern": r"@(ManyToOne|OneToMany|OneToOne|ManyToMany)",
            "reason": "Zero-Tolerance: JPA 관계 어노테이션 금지 (Long FK 전략)"
        }
    }

    def __init__(self, merged_issues: List[Dict]):
        """
        초기화

        Args:
            merged_issues: deduplicator.py에서 병합된 이슈 리스트
        """
        self.merged_issues = merged_issues
        self.prioritized_issues: List[PrioritizedIssue] = []

    def prioritize(self) -> List[PrioritizedIssue]:
        """
        우선순위 계산 실행

        Returns:
            우선순위가 결정된 이슈 리스트
        """
        print(f"\n🎯 우선순위 계산 시작: {len(self.merged_issues)}개 이슈")

        for issue in self.merged_issues:
            prioritized = self._calculate_priority(issue)
            self.prioritized_issues.append(prioritized)

        # 우선순위별 정렬 (Critical > Important > Suggestion)
        priority_order = {"Critical": 0, "Important": 1, "Suggestion": 2}
        self.prioritized_issues.sort(
            key=lambda x: (priority_order.get(x.priority, 3), -x.vote_count)
        )

        print(f"✅ 우선순위 계산 완료")
        self._print_priority_summary()

        return self.prioritized_issues

    def _calculate_priority(self, issue: Dict) -> PrioritizedIssue:
        """
        개별 이슈의 우선순위 계산

        Args:
            issue: 병합된 이슈

        Returns:
            우선순위가 결정된 이슈
        """
        vote_count = issue.get("vote_count", 1)
        description = issue.get("description", "")
        category = issue.get("category", "general")

        # 1. Zero-Tolerance 체크 (최우선)
        zero_tolerance_result = self._check_zero_tolerance(description)
        if zero_tolerance_result:
            return PrioritizedIssue(
                id=issue.get("id", ""),
                file=issue.get("file", ""),
                line=issue.get("line", 0),
                category=category,
                description=description,
                bots=issue.get("bots", []),
                vote_count=vote_count,
                priority="Critical",
                reason=zero_tolerance_result,
                effort=self._estimate_effort(description, "Critical"),
                zero_tolerance=True
            )

        # 2. 투표 시스템
        if vote_count == 3:
            priority = "Critical"
            reason = "3봇 합의 (Gemini + CodeRabbit + Codex)"
        elif vote_count == 2:
            priority = "Important"
            reason = f"2봇 합의 ({', '.join(issue.get('bots', []))})"
        else:
            priority = "Suggestion"
            reason = f"1봇만 ({issue.get('bots', ['Unknown'])[0]})"

        # 3. 카테고리 기반 우선순위 조정
        priority, reason = self._adjust_by_category(priority, reason, category, description)

        return PrioritizedIssue(
            id=issue.get("id", ""),
            file=issue.get("file", ""),
            line=issue.get("line", 0),
            category=category,
            description=description,
            bots=issue.get("bots", []),
            vote_count=vote_count,
            priority=priority,
            reason=reason,
            effort=self._estimate_effort(description, priority),
            zero_tolerance=False
        )

    def _check_zero_tolerance(self, description: str) -> str:
        """
        Zero-Tolerance 규칙 체크

        Args:
            description: 이슈 설명

        Returns:
            위반 시 이유, 아니면 빈 문자열
        """
        for rule_name, rule_data in self.ZERO_TOLERANCE_PATTERNS.items():
            pattern = rule_data["pattern"]
            if re.search(pattern, description, re.MULTILINE | re.DOTALL):
                return rule_data["reason"]

        return ""

    def _adjust_by_category(
        self,
        priority: str,
        reason: str,
        category: str,
        description: str
    ) -> tuple:
        """
        카테고리 기반 우선순위 조정

        Args:
            priority: 현재 우선순위
            reason: 현재 이유
            category: 카테고리
            description: 설명

        Returns:
            (조정된 우선순위, 조정된 이유)
        """
        description_lower = description.lower()

        # Security → 항상 Critical
        if category == "security":
            if "injection" in description_lower or "xss" in description_lower:
                return "Critical", "보안 취약점 (SQL Injection/XSS)"
            elif "credential" in description_lower or "password" in description_lower:
                return "Critical", "보안 취약점 (자격증명 노출)"

        # Performance → Critical/Important 판단
        if category == "performance":
            if any(kw in description_lower for kw in ["memory leak", "infinite loop", "deadlock"]):
                return "Critical", "심각한 성능 문제 (메모리 누수/데드락)"
            elif "slow" in description_lower or "optimization" in description_lower:
                if priority == "Suggestion":
                    return "Important", "성능 개선 가능"

        # Error Handling → Important
        if category == "error-handling":
            if "exception" in description_lower or "null pointer" in description_lower:
                if priority == "Suggestion":
                    return "Important", "오류 처리 누락"

        return priority, reason

    def _estimate_effort(self, description: str, priority: str) -> str:
        """
        작업 노력 추정

        Args:
            description: 이슈 설명
            priority: 우선순위

        Returns:
            S(Small), M(Medium), L(Large)
        """
        description_lower = description.lower()

        # 키워드 기반 추정
        if any(kw in description_lower for kw in ["refactor", "redesign", "restructure"]):
            return "L"  # Large (45-60분)
        elif any(kw in description_lower for kw in ["add", "implement", "create"]):
            return "M"  # Medium (20-40분)
        elif any(kw in description_lower for kw in ["fix", "remove", "rename", "update"]):
            return "S"  # Small (5-15분)

        # 우선순위 기반 기본값
        if priority == "Critical":
            return "S"  # Critical은 보통 빠른 수정
        elif priority == "Important":
            return "M"
        else:
            return "S"

    def _print_priority_summary(self) -> None:
        """우선순위 요약 출력"""
        critical = sum(1 for i in self.prioritized_issues if i.priority == "Critical")
        important = sum(1 for i in self.prioritized_issues if i.priority == "Important")
        suggestion = sum(1 for i in self.prioritized_issues if i.priority == "Suggestion")
        zero_tolerance = sum(1 for i in self.prioritized_issues if i.zero_tolerance)

        print(f"\n📊 우선순위 분포:")
        print(f"  ✅ Critical: {critical}개 (Zero-Tolerance: {zero_tolerance}개)")
        print(f"  ⚠️ Important: {important}개")
        print(f"  💡 Suggestion: {suggestion}개")

    def export_prioritized_issues(self) -> List[Dict]:
        """
        우선순위 이슈를 딕셔너리 리스트로 내보내기

        Returns:
            딕셔너리 리스트
        """
        return [
            {
                "id": issue.id,
                "file": issue.file,
                "line": issue.line,
                "category": issue.category,
                "description": issue.description,
                "bots": issue.bots,
                "vote_count": issue.vote_count,
                "priority": issue.priority,
                "reason": issue.reason,
                "effort": issue.effort,
                "zero_tolerance": issue.zero_tolerance
            }
            for issue in self.prioritized_issues
        ]


if __name__ == "__main__":
    """테스트용"""
    import json
    import sys

    if len(sys.argv) < 2:
        print("Usage: python prioritizer.py <merged_issues.json>")
        sys.exit(1)

    with open(sys.argv[1], 'r', encoding='utf-8') as f:
        merged_issues = json.load(f)

    prioritizer = Prioritizer(merged_issues)
    prioritized_issues = prioritizer.prioritize()

    # 결과 출력
    print("\n우선순위 이슈:")
    for issue in prioritized_issues[:10]:  # 처음 10개만
        print(f"\n[{issue.priority}] {issue.file}:{issue.line}")
        print(f"  봇: {', '.join(issue.bots)} (투표: {issue.vote_count})")
        print(f"  이유: {issue.reason}")
        print(f"  노력: {issue.effort}")
        if issue.zero_tolerance:
            print(f"  ⚠️ Zero-Tolerance 위반!")
