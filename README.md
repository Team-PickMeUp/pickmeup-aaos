# PickMeUp (AAOS)

Pleos Connect(Android Automotive OS) 차량 앱

- 운전자가 차량 화면에서 픽업 대상자에게 **위치 공유 요청**
- 공유받은 위치를 **경유지/목적지로 설정**해 픽업 진행

## 기술 스택

| 구분 | 내용 |
|---|---|
| 언어 | Kotlin 2.2 |
| UI | Jetpack Compose (BOM 2026.02.01), Material3 |
| 아키텍처 | **MVVM** (ViewModel + StateFlow), 단방향 데이터 흐름 |
| DI | Hilt |
| 화면 이동 | Navigation Compose (type-safe route) |
| 네트워크 | Retrofit 3, OkHttp 5, kotlinx.serialization |
| 빌드 | AGP 9.5, Gradle Version Catalog, KSP |
| 플랫폼 | Android Automotive OS, minSdk 34 / targetSdk 37 |
| 디자인 | [Figma 와이어프레임](https://www.figma.com/design/ViO6HZxZ5M1Vc2ulAEdzDF) |

## 아키텍처 (MVVM)

```
Screen (Compose) ── 이벤트 ──▶ ViewModel ──▶ Repository ──▶ API / SharedPreferences
       ▲                          │
       └──── UiState (StateFlow) ─┘
```

| 계층 | 역할 | 예시 |
|---|---|---|
| `XxxRoute` | ViewModel 연결, 화면 이동 처리 | `LocationRequestRoute` |
| `XxxScreen` | 상태를 받아 그리기만 함 (Preview 가능) | `LocationRequestScreen` |
| `XxxViewModel` | 화면 상태(`UiState`), 로직 | `LocationRequestViewModel` |
| `XxxRepository` | 데이터 출처 캡슐화 (interface + 구현) | `PickupRepository` |

- 상태 없는 화면(가이드, 설정, 약관 상세)은 ViewModel 없이 `Screen`만 존재
- 일회성 이벤트(화면 이동 등)는 ViewModel의 `Channel`로 전달
- 새 화면 추가: `Destinations.kt`에 경로(`XxxDestination`) 추가 → `PickMeUpNavHost.kt`에 `composable` 등록

## 프로젝트 구조

```
app/src/main/java/com/example/pickmeup/
├── PickMeUpApplication.kt   # Hilt 시작점
├── MainActivity.kt          # 약관 동의 여부로 시작 화면 결정
├── navigation/              # 화면 경로(Destinations), NavHost
├── di/                      # Hilt 모듈 (네트워크, 저장소 연결)
├── model/                   # 화면·데이터 계층 공용 모델
├── data/
│   ├── terms/               # 약관 동의 저장소 (SharedPreferences)
│   ├── pickup/              # 픽업 저장소 (Fake / Remote)
│   ├── target/              # 저장된 픽업 대상자 (SharedPreferences, 백업 제외)
│   └── remote/              # Retrofit API, DTO
└── ui/
    ├── design/              # Figma px → dp/sp 변환 (기준 너비 1700px)
    ├── theme/               # 색상, 폰트(Asta Sans), 텍스트 스타일
    ├── components/          # 공통 컴포넌트 (헤더, 설정 버튼)
    ├── terms/               # 약관 동의 / 약관 상세
    ├── target/              # 픽업 불러오기 (저장된 대상자 선택)
    ├── location/            # 위치 요청, 전화번호 검증
    ├── share/               # 공유 결과, 픽업 진행 중
    ├── guide/               # 앱 사용 가이드
    └── settings/            # 설정
```

## 라이브러리

- 버전은 [`gradle/libs.versions.toml`](gradle/libs.versions.toml)에서 일괄 관리
- 라이브러리 추가 시 위 파일에 등록하고 아래 표도 함께 갱신

| 구분 | 라이브러리 | 버전 | 용도 |
|---|---|---|---|
| UI | Jetpack Compose BOM | 2026.02.01 | Compose 라이브러리 버전 일괄 관리 |
| | Compose Material3 | (BOM) | Material 컴포넌트, 테마 |
| | Activity Compose | 1.13.0 | `setContent`로 Compose 화면 표시 |
| 아키텍처 | Lifecycle ViewModel Compose | 2.11.0 | ViewModel, `SavedStateHandle` |
| | Lifecycle Runtime Compose | 2.11.0 | `collectAsStateWithLifecycle` (화면이 보일 때만 상태 구독) |
| 화면 이동 | Navigation Compose | 2.10.2 | 화면 이동, type-safe route |
| DI | Hilt (Dagger) | 2.60.1 | 의존성 주입 |
| | Hilt Lifecycle ViewModel Compose | 1.4.0 | Compose에서 `hiltViewModel()` |
| 네트워크 | Retrofit | 3.0.0 | REST API 호출 |
| | Retrofit kotlinx.serialization Converter | 3.0.0 | Retrofit ↔ kotlinx.serialization 연결 |
| | OkHttp Logging Interceptor | 5.5.1 | debug 빌드 요청/응답 로그 |
| | kotlinx.serialization JSON | 1.11.0 | JSON 직렬화, Navigation 경로 직렬화 |
| 테스트 | JUnit 4 | 4.13.2 | 단위 테스트 |
| | kotlinx-coroutines-test | 1.11.0 | ViewModel(코루틴) 테스트 |

| 빌드 플러그인 | 버전 | 용도 |
|---|---|---|
| Android Gradle Plugin | 9.5.1 | 안드로이드 빌드 |
| Kotlin Compose / Serialization | 2.2.10 | Compose 컴파일러, `@Serializable` |
| KSP | 2.3.13 | Hilt 코드 생성 |
| Hilt Gradle Plugin | 2.60.1 | Hilt 설정 |

- kotlinx.serialization 1.12 이상은 Kotlin 2.4 필요 → 1.11.0 사용 (Kotlin 업그레이드 시 함께 올릴 것)

### 리소스

| 이름 | 용도 |
|---|---|
| Asta Sans (`res/font/asta_sans.ttf`) | Figma 디자인 폰트 (가변 폰트, SIL Open Font License) |

## 실행 방법

1. Android Studio로 프로젝트 열기
2. Pleos 에뮬레이터(API 34) 실행
3. `app` 구성 실행

```bash
./gradlew assembleDebug     # 빌드
./gradlew testDebugUnitTest # 단위 테스트
```

### API 설정 (`local.properties`)

| 키 | 기본값 | 설명 |
|---|---|---|
| `pickmeup.api.useFake` | `true` | `true`면 가짜 저장소 사용 (위치 요청이 항상 즉시 '허용') |
| `pickmeup.api.baseUrl` | `http://10.0.2.2:8080/` | API 주소 (기본값: 에뮬레이터에서 본 PC의 `localhost:8080`) |

- http(비암호화) 접속은 debug 빌드에서 `10.0.2.2`, `localhost`만 허용

### Pleos 에뮬레이터 참고사항

- 앱은 **사용자 10**으로 실행
- 데이터 초기화(약관 미동의 상태로 되돌리기):
  ```bash
  adb shell pm clear --user 10 com.example.pickmeup
  ```
- 상태바·GNB는 Pleos 시스템이 그림, 앱 영역은 약 1700×1184
- **Toast 표시 안 됨** → 사용자 안내는 화면 내 UI로 처리

## 협업 컨벤션

- 브랜치·커밋·PR·이슈 모두 **같은 타입** 사용 (아래 표 기준)
- git 사용 순서(처음 세팅, 매번 작업 순서, 충돌 해결): [GIT_GUIDE.md](GIT_GUIDE.md)

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
타입/작업-내용          # 기본
타입/이슈번호-작업-내용  # 이슈가 있을 때
```

- `main`: 항상 빌드되는 상태 유지, **직접 push 금지**, PR로만 병합
- 작업 브랜치는 `main`에서 생성, 영어 소문자와 하이픈(`-`)만 사용
- 예: `feat/pickup-in-progress`, `fix/12-keyboard-resize`, `docs/readme`

### 커밋

```
깃모지 타입: 요약

(선택) 본문 — 무엇을, 왜 바꿨는지
```

- 요약: 한국어 50자 이내, 마침표 없이 "~ 추가 / ~ 수정 / ~ 삭제"로 끝냄
- 한 커밋에 한 가지 변경만
- 이슈 연결 시 본문에 `#12`처럼 번호 기재

### Pull Request

- **제목**: 커밋과 같은 형식 → `✨ feat: 픽업 진행 중 화면 추가`
- **본문**: PR 템플릿 자동 입력, UI 변경 시 **에뮬레이터 스크린샷 필수**
- 이슈를 닫을 때 본문에 `Closes #12` 기재
- 리뷰어 **1명 이상 승인** 후 병합
- 병합은 **Squash and merge**만 허용 (PR 제목이 `main` 커밋 메시지가 됨)
- 병합 후 작업 브랜치 자동 삭제

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

- Android Studio 기본 Kotlin 코드 스타일 사용 (⌥⌘L 정렬)
- 문구는 하드코딩 금지, `res/values/strings.xml`에 작성 (큰따옴표는 `\"`로 이스케이프)
- Figma 수치(px)는 `24.px`, `34.pxSp`처럼 그대로 작성 (화면 너비에 맞춰 자동 변환)
- 텍스트 스타일은 `astaStyle(크기, 굵기, 색상, 행간배수)`(`ui/theme/Type.kt`), 색상은 `PickMeUpColors`(`ui/theme/Color.kt`) 사용
- 새 화면에는 Figma 노드 ID를 KDoc에 기재
