# 협업 규칙

## 브랜치

| 브랜치 | 용도 |
|---|---|
| `main` | 항상 빌드되는 상태. **직접 push 금지**, PR로만 병합 |
| `타입/작업명` | 작업 브랜치. `main`에서 따서 PR로 합칩니다 |

예시: `feat/pickup-api`, `fix/keyboard-resize`, `design/settings-icons`
이슈가 있으면 번호를 붙입니다: `feat/12-pickup-api`

## 커밋 메시지

```
타입: 요약 (50자 이내, 마침표 없이)

(선택) 본문 — 무엇을, 왜 바꿨는지
```

| 타입 | 언제 |
|---|---|
| `feat` | 새 기능·화면 |
| `fix` | 버그 수정 |
| `design` | UI·스타일만 변경 (동작 변화 없음) |
| `refactor` | 동작은 그대로, 코드 구조 개선 |
| `test` | 테스트 추가·수정 |
| `docs` | 문서 (README 등) |
| `chore` | 빌드 설정, 의존성, gitignore 등 |

예시:
```
feat: 픽업 진행 중 화면 추가
fix: 키보드가 올라오면 화면 전체가 축소되는 문제 수정
chore: Compose BOM 2026.02.01로 업데이트
```

- 한 커밋에는 한 가지 변경만 담습니다.
- 요약은 한국어로, "~ 추가 / ~ 수정 / ~ 삭제"처럼 끝냅니다.

## Pull Request

- 제목은 커밋 메시지와 같은 형식: `feat: 픽업 진행 중 화면 추가`
- PR 양식(자동으로 채워짐)을 작성합니다. UI 변경은 **에뮬레이터 스크린샷 필수**.
- 리뷰어 **1명 이상 승인** 후 병합합니다.
- 병합 방식은 **Squash and merge** (main 히스토리를 PR 단위로 깔끔하게 유지)
- 병합 후 작업 브랜치는 삭제합니다.

## 코드

- Android Studio 기본 Kotlin 코드 스타일을 따릅니다. (⌥⌘L 정렬)
- 문구는 하드코딩하지 않고 `strings.xml`에 둡니다.
- Figma 수치는 `Number.px` / `Number.pxSp`로 씁니다. 새 화면에는 Figma 노드 ID를 KDoc에 적어 둡니다.
