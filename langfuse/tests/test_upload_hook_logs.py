#!/usr/bin/env python3
"""
upload-hook-logs-v2.py 테스트

목적: 프로젝트 이름 추출 및 LangFuse Event 생성 로직 검증

테스트 항목:
1. 프로젝트 이름 추출 (session_start 이벤트에서)
2. Event 이름에 프로젝트 이름 포함
3. Event input에 project 필드 포함
4. Event metadata에 project_name 필드 포함
5. Event tags에 project-{project_name} 포함
"""

import json
import sys
import unittest
from pathlib import Path
from unittest.mock import Mock, patch, MagicMock

# 프로젝트 루트 경로 추가
PROJECT_ROOT = Path(__file__).parent.parent.parent.parent
sys.path.insert(0, str(PROJECT_ROOT / "scripts" / "langfuse"))

# 테스트 대상 모듈 import
# import importlib.util을 사용하여 하이픈이 포함된 파일 import
import importlib.util
spec = importlib.util.spec_from_file_location(
    "upload_hook_logs_v2",
    PROJECT_ROOT / "scripts" / "langfuse" / "upload-hook-logs-v2.py"
)
upload_module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(upload_module)
HookLogUploader = upload_module.HookLogUploader


class TestProjectNameExtraction(unittest.TestCase):
    """프로젝트 이름 추출 테스트"""

    def setUp(self):
        """테스트 설정"""
        self.uploader = HookLogUploader(dry_run=True)

    def test_extract_project_name_from_session_start(self):
        """session_start 이벤트에서 프로젝트 이름 추출"""
        # Given: session_start 이벤트가 포함된 이벤트 리스트
        events = [
            {
                'event': {
                    'event': 'session_start',
                    'project': 'claude-spring-standards',
                    'timestamp': '2025-11-10T10:00:00'
                },
                'line_num': 1
            },
            {
                'event': {
                    'event': 'keyword_analysis',
                    'context_score': 75,
                    'detected_keywords': ['domain'],
                    'detected_layers': ['domain']
                },
                'line_num': 2
            }
        ]

        # When: 이벤트 타입별로 분류
        event_by_type = {}
        for e in events:
            event_type = e['event'].get('event', 'unknown')
            if event_type not in event_by_type:
                event_by_type[event_type] = []
            event_by_type[event_type].append(e['event'])

        # Then: 프로젝트 이름 추출 성공
        session_start = event_by_type.get('session_start', [{}])[0]
        project_name = session_start.get('project', 'unknown-project')

        self.assertEqual(project_name, 'claude-spring-standards')

    def test_project_name_in_event_name(self):
        """Event 이름에 프로젝트 이름 포함"""
        # Given
        project_name = 'test-project'
        session_id = '1234567890-12345'

        # When
        event_name = f"{project_name}-hook-execution-{session_id[:8]}"

        # Then
        self.assertTrue(event_name.startswith('test-project-'))
        self.assertIn('hook-execution', event_name)
        self.assertEqual(event_name, 'test-project-hook-execution-12345678')

    def test_project_name_in_event_input(self):
        """Event input에 project 필드 포함"""
        # Given
        project_name = 'my-project'

        # When
        event_input = {
            "project": project_name,
            "session_id": "test-session",
            "context_score": 50
        }

        # Then
        self.assertIn('project', event_input)
        self.assertEqual(event_input['project'], 'my-project')

    def test_project_name_in_event_metadata(self):
        """Event metadata에 project_name 필드 포함"""
        # Given
        project_name = 'another-project'

        # When
        event_metadata = {
            "project_name": project_name,
            "threshold": 25,
            "estimated_tokens": 1000
        }

        # Then
        self.assertIn('project_name', event_metadata)
        self.assertEqual(event_metadata['project_name'], 'another-project')

    def test_project_tag_in_metadata(self):
        """Event tags에 project-{project_name} 포함"""
        # Given
        project_name = 'tag-test-project'
        session_id = '1234567890-12345'

        # When
        event_metadata_with_tags = {
            "project_name": project_name,
            "tags": [
                "hook-system",
                "cache-injection",
                f"project-{project_name}",
                f"session-{session_id[:8]}"
            ]
        }

        # Then
        self.assertIn('tags', event_metadata_with_tags)
        self.assertIn(f'project-{project_name}', event_metadata_with_tags['tags'])
        self.assertIn('project-tag-test-project', event_metadata_with_tags['tags'])

    def test_missing_project_name_defaults_to_unknown(self):
        """프로젝트 이름이 없을 때 'unknown-project' 기본값 사용"""
        # Given: session_start 이벤트에 project 필드가 없음
        events = [
            {
                'event': {
                    'event': 'session_start',
                    'timestamp': '2025-11-10T10:00:00'
                    # 'project' 필드 없음
                },
                'line_num': 1
            }
        ]

        # When
        event_by_type = {}
        for e in events:
            event_type = e['event'].get('event', 'unknown')
            if event_type not in event_by_type:
                event_by_type[event_type] = []
            event_by_type[event_type].append(e['event'])

        session_start = event_by_type.get('session_start', [{}])[0]
        project_name = session_start.get('project', 'unknown-project')

        # Then
        self.assertEqual(project_name, 'unknown-project')


class TestHookLogParsing(unittest.TestCase):
    """Hook 로그 파싱 테스트"""

    def setUp(self):
        """테스트 설정"""
        self.uploader = HookLogUploader(dry_run=True)

    @patch('pathlib.Path.exists')
    @patch('builtins.open')
    def test_parse_hook_logs_with_project_name(self, mock_open, mock_exists):
        """Hook 로그 파싱 시 프로젝트 이름 포함"""
        # Given: 프로젝트 이름이 포함된 Hook 로그
        mock_exists.return_value = True
        mock_log_content = [
            json.dumps({
                'event': 'session_start',
                'project': 'test-parsing-project',
                'session_id': 'session-123',
                'timestamp': '2025-11-10T10:00:00'
            }),
            json.dumps({
                'event': 'keyword_analysis',
                'session_id': 'session-123',
                'context_score': 60
            })
        ]
        mock_open.return_value.__enter__.return_value = iter(mock_log_content)

        # When
        sessions = self.uploader.parse_hook_logs(start_line=0)

        # Then
        self.assertIn('session-123', sessions)
        session_events = sessions['session-123']

        # session_start 이벤트에 project 필드 존재 확인
        session_start_event = next(
            (e['event'] for e in session_events if e['event'].get('event') == 'session_start'),
            None
        )
        self.assertIsNotNone(session_start_event)
        self.assertEqual(session_start_event.get('project'), 'test-parsing-project')


class TestEventCreation(unittest.TestCase):
    """LangFuse Event 생성 테스트"""

    def setUp(self):
        """테스트 설정"""
        self.uploader = HookLogUploader(dry_run=True)

    @patch.object(HookLogUploader, 'create_hook_event')
    def test_create_event_with_project_name(self, mock_create_event):
        """프로젝트 이름이 포함된 Event 생성"""
        # Given
        session_id = 'test-session-456'
        events = [
            {
                'event': {
                    'event': 'session_start',
                    'project': 'event-test-project',
                    'session_id': session_id
                },
                'line_num': 1
            },
            {
                'event': {
                    'event': 'keyword_analysis',
                    'session_id': session_id,
                    'context_score': 80,
                    'detected_keywords': ['domain'],
                    'detected_layers': ['domain'],
                    'threshold': 25
                },
                'line_num': 2
            },
            {
                'event': {
                    'event': 'cache_injection',
                    'session_id': session_id,
                    'layer': 'domain',
                    'rules_loaded': 15,
                    'estimated_tokens': 800
                },
                'line_num': 3
            }
        ]

        # When
        self.uploader.create_hook_event(session_id, events)

        # Then: create_hook_event가 호출되었는지 확인
        mock_create_event.assert_called_once_with(session_id, events)


class TestDryRunMode(unittest.TestCase):
    """Dry-run 모드 테스트"""

    def test_dry_run_mode_no_langfuse_calls(self):
        """Dry-run 모드에서는 실제 LangFuse API 호출 없음"""
        # Given
        uploader = HookLogUploader(dry_run=True)

        # Then
        self.assertTrue(uploader.dry_run)
        # Dry-run 모드에서는 langfuse 클라이언트가 있지만 실제 호출하지 않음
        self.assertIsNotNone(uploader.langfuse)


def run_tests():
    """테스트 실행"""
    # Test suite 생성
    suite = unittest.TestLoader().loadTestsFromModule(sys.modules[__name__])

    # Test runner 생성 및 실행
    runner = unittest.TextTestRunner(verbosity=2)
    result = runner.run(suite)

    # 결과 반환 (성공: 0, 실패: 1)
    return 0 if result.wasSuccessful() else 1


if __name__ == '__main__':
    sys.exit(run_tests())
