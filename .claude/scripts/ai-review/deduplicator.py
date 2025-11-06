#!/usr/bin/env python3
"""
AI Review Deduplicator

여러 AI 봇의 댓글 중복 제거
- 파일:라인 위치 매칭
- 텍스트 유사도 계산 (TF-IDF 코사인 유사도)
- 카테고리 매칭
- Similarity > 0.8 시 병합
"""

import re
from typing import List, Dict, Set, Tuple
from dataclasses import dataclass, field
from collections import Counter
import math


@dataclass
class MergedIssue:
    """병합된 이슈"""
    id: str  # 대표 ID
    file: str
    line: int
    category: str
    description: str
    bots: List[str] = field(default_factory=list)  # 이 이슈를 제기한 봇들
    vote_count: int = 0  # 투표 수 (1-3)
    priority: str = ""  # Critical, Important, Suggestion
    similarity_score: float = 1.0  # 병합된 댓글들의 평균 유사도


class Deduplicator:
    """중복 제거 및 병합 클래스"""

    SIMILARITY_THRESHOLD = 0.8  # 유사도 임계값

    def __init__(self, comments: List[Dict]):
        """
        초기화

        Args:
            comments: fetch_reviews.py에서 가져온 댓글 리스트
        """
        self.comments = comments
        self.merged_issues: List[MergedIssue] = []

    def deduplicate(self) -> List[MergedIssue]:
        """
        중복 제거 및 병합 실행

        Returns:
            병합된 이슈 리스트
        """
        print(f"\n🔄 중복 제거 시작: {len(self.comments)}개 댓글")

        # 1. 위치별로 그룹화 (file:line)
        location_groups = self._group_by_location()

        # 2. 각 그룹 내에서 유사도 계산 및 병합
        for location, group_comments in location_groups.items():
            if len(group_comments) == 1:
                # 단일 댓글 → 바로 추가
                comment = group_comments[0]
                self.merged_issues.append(self._create_merged_issue([comment]))
            else:
                # 다중 댓글 → 유사도 계산 후 병합
                merged = self._merge_similar_comments(group_comments)
                self.merged_issues.extend(merged)

        # 3. 투표 수 계산
        for issue in self.merged_issues:
            issue.vote_count = len(set(issue.bots))

        print(f"✅ 중복 제거 완료: {len(self.merged_issues)}개 이슈")
        print(f"  병합률: {((len(self.comments) - len(self.merged_issues)) / len(self.comments) * 100):.1f}%")

        return self.merged_issues

    def _group_by_location(self) -> Dict[str, List[Dict]]:
        """파일:라인 위치별로 댓글 그룹화"""
        groups = {}

        for comment in self.comments:
            file = comment.get("file") or "general"
            line = comment.get("line") or 0
            location_key = f"{file}:{line}"

            if location_key not in groups:
                groups[location_key] = []
            groups[location_key].append(comment)

        return groups

    def _merge_similar_comments(self, comments: List[Dict]) -> List[MergedIssue]:
        """
        유사한 댓글 병합

        Args:
            comments: 같은 위치의 댓글 리스트

        Returns:
            병합된 이슈 리스트
        """
        merged_groups: List[List[Dict]] = []
        used_indices: Set[int] = set()

        for i, comment1 in enumerate(comments):
            if i in used_indices:
                continue

            current_group = [comment1]
            used_indices.add(i)

            for j, comment2 in enumerate(comments[i+1:], start=i+1):
                if j in used_indices:
                    continue

                similarity = self._calculate_similarity(comment1, comment2)
                if similarity > self.SIMILARITY_THRESHOLD:
                    current_group.append(comment2)
                    used_indices.add(j)

            merged_groups.append(current_group)

        return [self._create_merged_issue(group) for group in merged_groups]

    def _calculate_similarity(self, comment1: Dict, comment2: Dict) -> float:
        """
        두 댓글 간 유사도 계산

        Args:
            comment1: 첫 번째 댓글
            comment2: 두 번째 댓글

        Returns:
            유사도 (0.0-1.0)
        """
        # 1. 위치 유사도 (같은 위치면 1.0)
        location_sim = 1.0 if (
            comment1.get("file") == comment2.get("file") and
            comment1.get("line") == comment2.get("line")
        ) else 0.0

        # 2. 텍스트 유사도 (TF-IDF 코사인)
        text1 = comment1.get("body", "")
        text2 = comment2.get("body", "")
        text_sim = self._cosine_similarity(text1, text2)

        # 3. 카테고리 유사도
        category_sim = 1.0 if comment1.get("category") == comment2.get("category") else 0.0

        # 가중 평균 (위치 50%, 텍스트 30%, 카테고리 20%)
        similarity = (
            location_sim * 0.5 +
            text_sim * 0.3 +
            category_sim * 0.2
        )

        return similarity

    def _cosine_similarity(self, text1: str, text2: str) -> float:
        """
        TF-IDF 코사인 유사도 계산 (간단 버전)

        Args:
            text1: 첫 번째 텍스트
            text2: 두 번째 텍스트

        Returns:
            코사인 유사도 (0.0-1.0)
        """
        # 토큰화 (공백, 특수문자 기준)
        tokens1 = self._tokenize(text1.lower())
        tokens2 = self._tokenize(text2.lower())

        if not tokens1 or not tokens2:
            return 0.0

        # TF (Term Frequency)
        tf1 = Counter(tokens1)
        tf2 = Counter(tokens2)

        # 공통 토큰
        common_tokens = set(tf1.keys()) & set(tf2.keys())

        if not common_tokens:
            return 0.0

        # 내적 계산
        dot_product = sum(tf1[token] * tf2[token] for token in common_tokens)

        # 크기 계산
        magnitude1 = math.sqrt(sum(count ** 2 for count in tf1.values()))
        magnitude2 = math.sqrt(sum(count ** 2 for count in tf2.values()))

        if magnitude1 == 0 or magnitude2 == 0:
            return 0.0

        return dot_product / (magnitude1 * magnitude2)

    def _tokenize(self, text: str) -> List[str]:
        """
        텍스트 토큰화

        Args:
            text: 입력 텍스트

        Returns:
            토큰 리스트
        """
        # 알파벳, 숫자만 추출
        tokens = re.findall(r'\b\w+\b', text)
        # 불용어 제거 (간단 버전)
        stopwords = {"the", "a", "an", "is", "it", "to", "of", "and", "in", "for"}
        return [token for token in tokens if token not in stopwords and len(token) > 2]

    def _create_merged_issue(self, comments: List[Dict]) -> MergedIssue:
        """
        댓글 그룹을 병합된 이슈로 변환

        Args:
            comments: 병합할 댓글 리스트

        Returns:
            병합된 이슈
        """
        # 대표 댓글 (첫 번째)
        representative = comments[0]

        # 모든 봇 수집
        bots = list(set(comment.get("bot_name", "unknown") for comment in comments))

        # 설명 병합 (가장 긴 것 선택)
        descriptions = [comment.get("body", "") for comment in comments]
        merged_description = max(descriptions, key=len)

        return MergedIssue(
            id=representative.get("id", "unknown"),
            file=representative.get("file", "general"),
            line=representative.get("line", 0),
            category=representative.get("category", "general"),
            description=merged_description,
            bots=bots,
            vote_count=len(set(bots)),
            similarity_score=1.0 if len(comments) == 1 else 0.9  # 임시
        )

    def show_deduplication_report(self) -> None:
        """중복 제거 리포트 출력"""
        print("\n📊 중복 제거 리포트")
        print("=" * 60)
        print(f"원본 댓글 수: {len(self.comments)}")
        print(f"병합 후 이슈 수: {len(self.merged_issues)}")
        print(f"병합률: {((len(self.comments) - len(self.merged_issues)) / len(self.comments) * 100):.1f}%")

        # 투표 분포
        vote_distribution = Counter(issue.vote_count for issue in self.merged_issues)
        print("\n투표 분포:")
        for vote_count in sorted(vote_distribution.keys(), reverse=True):
            count = vote_distribution[vote_count]
            print(f"  {vote_count}봇 합의: {count}개")


if __name__ == "__main__":
    """테스트용"""
    import json
    import sys

    if len(sys.argv) < 2:
        print("Usage: python deduplicator.py <comments.json>")
        sys.exit(1)

    with open(sys.argv[1], 'r', encoding='utf-8') as f:
        data = json.load(f)
        comments = data.get("comments", [])

    dedup = Deduplicator(comments)
    merged_issues = dedup.deduplicate()
    dedup.show_deduplication_report()

    # 결과 출력
    print("\n병합된 이슈:")
    for issue in merged_issues[:5]:  # 처음 5개만
        print(f"\n{issue.file}:{issue.line} ({issue.category})")
        print(f"  봇: {', '.join(issue.bots)} (투표: {issue.vote_count})")
        print(f"  설명: {issue.description[:100]}...")
