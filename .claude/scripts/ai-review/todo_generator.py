#!/usr/bin/env python3
"""
AI Review TodoList Generator

우선순위별로 정리된 이슈를 TodoList 형식으로 변환
- Critical → High Priority (Must-Fix)
- Important → Medium Priority (Should-Fix)
- Suggestion → Low Priority (Nice-to-Have)
"""

from typing import List, Dict
from dataclasses import dataclass


@dataclass
class TodoItem:
    """TodoList 항목"""
    priority: str  # High, Medium, Low
    title: str
    file: str
    line: int
    effort: str  # S, M, L
    reason: str
    bots: List[str]
    zero_tolerance: bool = False


class TodoGenerator:
    """TodoList 생성기"""

    # 노력 → 시간 매핑
    EFFORT_TIME = {
        "S": "5-15분",
        "M": "20-40분",
        "L": "45-60분"
    }

    # 우선순위 → 이모지
    PRIORITY_EMOJI = {
        "High": "✅",
        "Medium": "⚠️",
        "Low": "💡"
    }

    def __init__(self, prioritized_issues: List[Dict]):
        """
        초기화

        Args:
            prioritized_issues: prioritizer.py에서 우선순위가 결정된 이슈 리스트
        """
        self.prioritized_issues = prioritized_issues
        self.todo_items: List[TodoItem] = []

    def generate(self) -> str:
        """
        TodoList 생성

        Returns:
            마크다운 형식의 TodoList 문자열
        """
        print(f"\n📝 TodoList 생성 시작: {len(self.prioritized_issues)}개 이슈")

        # 이슈 → TodoItem 변환
        for issue in self.prioritized_issues:
            self.todo_items.append(self._create_todo_item(issue))

        # TodoList 마크다운 생성
        todo_markdown = self._build_markdown()

        print(f"✅ TodoList 생성 완료")
        self._print_summary()

        return todo_markdown

    def _create_todo_item(self, issue: Dict) -> TodoItem:
        """
        이슈를 TodoItem으로 변환

        Args:
            issue: 우선순위가 결정된 이슈

        Returns:
            TodoItem
        """
        priority_map = {
            "Critical": "High",
            "Important": "Medium",
            "Suggestion": "Low"
        }

        priority = priority_map.get(issue["priority"], "Low")

        # 제목 생성 (파일명 + 간단한 설명)
        file_name = issue["file"].split("/")[-1] if issue["file"] else "general"
        description_short = issue["description"][:80].replace("\n", " ")
        title = f"Fix {file_name}:{issue['line']} - {description_short}"

        return TodoItem(
            priority=priority,
            title=title,
            file=issue["file"],
            line=issue["line"],
            effort=issue["effort"],
            reason=issue["reason"],
            bots=issue["bots"],
            zero_tolerance=issue.get("zero_tolerance", False)
        )

    def _build_markdown(self) -> str:
        """
        마크다운 TodoList 생성

        Returns:
            마크다운 문자열
        """
        lines = []

        # 헤더
        lines.append("# 🤖 AI Review TodoList\n")
        lines.append("AI 봇(Gemini, CodeRabbit, Codex) 리뷰를 기반으로 생성된 우선순위별 작업 목록입니다.\n")

        # 통계
        high_count = sum(1 for item in self.todo_items if item.priority == "High")
        medium_count = sum(1 for item in self.todo_items if item.priority == "Medium")
        low_count = sum(1 for item in self.todo_items if item.priority == "Low")
        zero_tolerance_count = sum(1 for item in self.todo_items if item.zero_tolerance)

        lines.append("## 📊 요약\n")
        lines.append(f"- ✅ High Priority (Must-Fix): **{high_count}개**")
        if zero_tolerance_count > 0:
            lines.append(f"  - ⚠️ Zero-Tolerance 위반: **{zero_tolerance_count}개**")
        lines.append(f"- ⚠️ Medium Priority (Should-Fix): **{medium_count}개**")
        lines.append(f"- 💡 Low Priority (Nice-to-Have): **{low_count}개**")
        lines.append(f"- **총 {len(self.todo_items)}개 작업**\n")

        # High Priority
        high_items = [item for item in self.todo_items if item.priority == "High"]
        if high_items:
            lines.append("## ✅ High Priority (Must-Fix)\n")
            lines.append("**즉시 수정 필요** - Zero-Tolerance 위반 또는 3봇 합의\n")
            for i, item in enumerate(high_items, 1):
                lines.extend(self._format_todo_item(i, item))
            lines.append("")

        # Medium Priority
        medium_items = [item for item in self.todo_items if item.priority == "Medium"]
        if medium_items:
            lines.append("## ⚠️ Medium Priority (Should-Fix)\n")
            lines.append("**권장 수정** - 2봇 합의 또는 중요한 개선사항\n")
            for i, item in enumerate(medium_items, 1):
                lines.extend(self._format_todo_item(i, item))
            lines.append("")

        # Low Priority
        low_items = [item for item in self.todo_items if item.priority == "Low"]
        if low_items:
            lines.append("## 💡 Low Priority (Nice-to-Have)\n")
            lines.append("**선택적 개선** - 1봇만 제안 또는 스타일 개선\n")
            for i, item in enumerate(low_items, 1):
                lines.extend(self._format_todo_item(i, item))
            lines.append("")

        return "\n".join(lines)

    def _format_todo_item(self, index: int, item: TodoItem) -> List[str]:
        """
        TodoItem을 마크다운 형식으로 포맷

        Args:
            index: 항목 번호
            item: TodoItem

        Returns:
            마크다운 라인 리스트
        """
        lines = []

        # 체크박스 + 제목
        emoji = self.PRIORITY_EMOJI[item.priority]
        zt_marker = " ⚠️ **Zero-Tolerance**" if item.zero_tolerance else ""
        lines.append(f"### {emoji} {index}. {item.title}{zt_marker}\n")

        # 위치
        lines.append(f"**📍 위치**: `{item.file}:{item.line}`")

        # 봇 정보
        bot_names = ", ".join(item.bots)
        vote_count = len(set(item.bots))
        lines.append(f"**🤖 봇**: {bot_names} (투표: {vote_count})")

        # 이유
        lines.append(f"**💡 이유**: {item.reason}")

        # 예상 시간
        time_estimate = self.EFFORT_TIME.get(item.effort, "알 수 없음")
        lines.append(f"**⏱️ 예상 시간**: {time_estimate}\n")

        return lines

    def _print_summary(self) -> None:
        """TodoList 요약 출력"""
        high = sum(1 for item in self.todo_items if item.priority == "High")
        medium = sum(1 for item in self.todo_items if item.priority == "Medium")
        low = sum(1 for item in self.todo_items if item.priority == "Low")

        print(f"  ✅ High: {high}개")
        print(f"  ⚠️ Medium: {medium}개")
        print(f"  💡 Low: {low}개")

    def save_to_file(self, output_file: str, markdown: str) -> None:
        """
        TodoList를 파일로 저장

        Args:
            output_file: 출력 파일 경로
            markdown: 마크다운 내용
        """
        with open(output_file, 'w', encoding='utf-8') as f:
            f.write(markdown)

        print(f"💾 TodoList 저장: {output_file}")


if __name__ == "__main__":
    """테스트용"""
    import json
    import sys

    if len(sys.argv) < 2:
        print("Usage: python todo_generator.py <prioritized_issues.json> [output.md]")
        sys.exit(1)

    with open(sys.argv[1], 'r', encoding='utf-8') as f:
        prioritized_issues = json.load(f)

    generator = TodoGenerator(prioritized_issues)
    todo_markdown = generator.generate()

    if len(sys.argv) > 2:
        generator.save_to_file(sys.argv[2], todo_markdown)
    else:
        print("\n" + todo_markdown)
