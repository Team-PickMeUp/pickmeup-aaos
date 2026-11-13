# PickMeUp (AAOS)

운전자가 차량 화면에서 **픽업 대상자에게 위치 공유를 요청**하고, 공유받은 위치를 **경유지/목적지로 설정**해 픽업까지 진행하는 Pleos Connect(Android Automotive OS) 차량 앱입니다.

## 기술 스택

| 구분 | 내용 |
|---|---|
| 언어 | Kotlin 2.2 |
| UI | Jetpack Compose (BOM 2026.02.01), Material3 |
| 빌드 | AGP 9.5, Gradle Version Catalog |
| 플랫폼 | Android Automotive OS, minSdk 34 / targetSdk 37 |
| 디자인 | [Figma 와이어프레임](https://www.figma.com/design/ViO6HZxZ5M1Vc2ulAEdzDF) |

## 실행 방법

1. Android Studio로 프로젝트를 엽니다.
2. Pleos 에뮬레이터(API 34)를 실행합니다.
3. `app` 구성을 실행합니다.

```bash
./gradlew assembleDebug     # 빌드
./gradlew testDebugUnitTest # 단위 테스트
```

### Pleos 에뮬레이터 참고사항

- 앱은 **사용자 10**으로 실행됩니다. 데이터 초기화(약관 미동의 상태로 되돌리기)는:
  ```bash
  adb shell pm clear --user 10 com.example.pickmeup
  ```
- 상태바·GNB는 Pleos 시스템이 그리며, 앱 영역은 약 1700×1184입니다.
- **Toast가 보이지 않습니다.** 사용자 피드백은 화면 내 UI로 처리해야 합니다.

## 프로젝트 구조

```
app/src/main/java/com/example/pickmeup/
├── MainActivity.kt          # 약관 동의 여부 분기 + 화면 이동(back stack)
├── data/                    # 약관 동의 저장소 (SharedPreferences)
└── ui/
    ├── design/              # Figma px → dp/sp 변환 (LocalPxScale, 기준 너비 1700px)
    ├── theme/               # 색상, 폰트(Asta Sans)
    ├── components/          # 공통 컴포넌트 (헤더, 설정 버튼)
    ├── terms/               # 약관 동의 / 약관 상세
    ├── main/                # 메인
    ├── location/            # 위치 요청, 전화번호 검증
    ├── share/               # 공유 결과, 픽업 진행 중
    ├── guide/               # 앱 사용 가이드
    └── settings/            # 설정
```

### UI 작업 규칙

- Figma 수치(px)는 그대로 `24.px`, `34.pxSp`처럼 씁니다. 화면 너비에 맞춰 자동 변환됩니다.
- 모든 문구는 `res/values/strings.xml`에 둡니다. (큰따옴표는 `\"`로 이스케이프)

## 남은 작업

- [ ] 위치 요청 결과 실제 연동 (현재는 '허용'과 임시 주소로 고정)
- [ ] 경유지/목적지 실제 내비게이션 설정 (현재는 화면 이동만)
- [ ] 픽업 대상자 지정 화면
- [ ] 서비스 이용약관 본문
- [ ] 패키지명 확정 (`com.example.pickmeup` → 정식 이름)

## 협업 컨벤션

브랜치·커밋·PR·이슈는 모두 **같은 타입**을 씁니다. 아래 표 하나만 기억하면 됩니다.

### 타입 & 깃모지

| 깃모지 | 타입 | 언제 | 예시 |
|:---:|---|---|---|
| ✨ | `feat` | 새 기능·화면 추가 | `✨ feat: 픽업 진행 중 화면 추가` |
| 🐛 | `fix` | 버그 수정 | `🐛 fix: 키보드가 올라오면 화면이 축소되는 문제 수정` |
| 🚑️ | `hotfix` | `main`에서 발견된 급한 버그 수정 | `🚑️ hotfix: 앱 실행 시 크래시 수정` |
| 💄 | `design` | UI·스타일만 변경 (동작 변화 없음) | `💄 design: 설정 화면 아이콘 크기 조정` |
| ♻️ | `refactor` | 동작은 그대로, 코드 구조 개선 | `♻️ refactor: 공유 화면 공통 카드 컴포넌트로 분리` |
| 🔥 | `remove` | 코드·파일 삭제 | `🔥 remove: 사용하지 않는 아이콘 삭제` |
| ✅ | `test` | 테스트 추가·수정 | `✅ test: 전화번호 검증 테스트 추가` |
| 📝 | `docs` | 문서 (README 등) | `📝 docs: 실행 방법 추가` |
| 🔧 | `chore` | 빌드 설정, 의존성, gitignore 등 | `🔧 chore: Compose BOM 업데이트` |

### 브랜치

```
타입/작업-내용        # 기본
타입/이슈번호-작업-내용  # 이슈가 있을 때
```

- `main`: 항상 빌드되는 상태. **직접 push 금지**, PR로만 병합합니다.
- 작업 브랜치는 `main`에서 만들고, 영어 소문자와 하이픈(`-`)만 씁니다.
- 예: `feat/pickup-in-progress`, `fix/12-keyboard-resize`, `docs/readme`

### 커밋

```
깃모지 타입: 요약

(선택) 본문 — 무엇을, 왜 바꿨는지
```

- 요약은 한국어로 50자 이내, 마침표 없이 "~ 추가 / ~ 수정 / ~ 삭제"로 끝냅니다.
- 한 커밋에는 한 가지 변경만 담습니다.
- 이슈와 연결할 때는 본문에 `#12`처럼 번호를 적습니다.

### Pull Request

- **제목**: 커밋과 같은 형식 → `✨ feat: 픽업 진행 중 화면 추가`
- **본문**: PR 템플릿이 자동으로 채워집니다. UI 변경은 **에뮬레이터 스크린샷 필수**.
- 이슈를 닫을 때는 본문에 `Closes #12`를 적습니다.
- 리뷰어 **1명 이상 승인** 후 병합합니다.
- 병합은 **Squash and merge**만 허용됩니다. (PR 제목이 그대로 `main`의 커밋 메시지가 됩니다)
- 병합 후 작업 브랜치는 자동 삭제됩니다.

### 이슈

- **제목**: `깃모지 타입: 요약` → `✨ feat: 픽업 대상자 지정 화면`, `🐛 fix: 뒤로가기 시 입력값 사라짐`
- 템플릿: **기능 요청**(✨ feat) / **버그 제보**(🐛 fix)

### 흐름 한눈에

```
이슈 생성        ✨ feat: 픽업 대상자 지정 화면  (#12)
  ↓
브랜치 생성      feat/12-pickup-target
  ↓
커밋            ✨ feat: 픽업 대상자 목록 UI 추가
                💄 design: 목록 간격 Figma에 맞게 조정
  ↓
PR 생성         ✨ feat: 픽업 대상자 지정 화면 추가   (본문: Closes #12)
  ↓
리뷰 승인 → Squash and merge → 브랜치 자동 삭제
```

### 코드

- Android Studio 기본 Kotlin 코드 스타일을 따릅니다. (⌥⌘L 정렬)
- 문구는 하드코딩하지 않고 `strings.xml`에 둡니다.
- 새 화면에는 Figma 노드 ID를 KDoc에 적어 둡니다.
