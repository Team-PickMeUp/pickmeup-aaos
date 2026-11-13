# PickMeUp 🚗 (AAOS)

운전자가 차량 화면에서 **픽업 대상자에게 위치 공유를 요청**하고, 공유받은 위치를 **경유지/목적지로 설정**해 픽업까지 진행하는 Pleos Connect(Android Automotive OS) 차량 앱입니다.

> 서버: [`pickmeup-server`](https://github.com/Team-PickMeUp/pickmeup-server) (예정)

## 화면 흐름

```
최초 실행 ─ 약관 동의 ─┐
                       ▼
                     메인 ─┬─ 위치 요청 ─ 공유 결과 ─┬─ 허용 ─ 경유지/목적지 설정 ─ 픽업 진행 중 ─ 완료/취소 → 메인
                           │                         └─ 미허용
                           ├─ 앱 사용 가이드
                           └─ 설정 ─┬─ 앱 사용 가이드
                                    ├─ 픽업 대상자 지정 (준비 중)
                                    ├─ 위치기반서비스 이용약관
                                    └─ 개인정보 처리방침
```

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
2. Pleos Connect 에뮬레이터(API 34)를 실행합니다.
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

- [ ] 위치 공유 요청 서버 연동 (SMS 발송, 응답 대기 화면)
- [ ] Pleos Connect SDK로 경유지/목적지 실제 설정
- [ ] 픽업 완료/취소 서버 알림
- [ ] 픽업 대상자 지정 화면
- [ ] 서비스 이용약관 본문
- [ ] 패키지명 확정 (`com.example.pickmeup` → 정식 이름)

## 협업 규칙

커밋·브랜치·PR 규칙은 [CONTRIBUTING.md](CONTRIBUTING.md)를 참고하세요.
