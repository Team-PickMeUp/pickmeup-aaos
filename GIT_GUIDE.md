# Git 작업 가이드

- 우리 프로젝트에서 git을 쓰는 순서 정리
- 처음 한 번: **0. 처음 세팅**
- 작업할 때마다: **1 → 2 → 3 → 4** 반복
- 브랜치·커밋 이름 규칙의 원본은 [README > 협업 컨벤션](README.md#협업-컨벤션)

> **규칙 요약**
> - `main`: 항상 빌드되고 동작하는 상태, **직접 커밋/푸시 금지**
> - 작업은 항상 새 브랜치에서 → PR(Pull Request)로 `main`에 합치기
> - 브랜치 이름: `타입/작업-내용` (예: `feat/pickup-target`, `fix/keyboard-resize`)
> - 커밋 메시지: `깃모지 타입: 요약` (예: `✨ feat: 픽업 대상자 목록 UI 추가`)

---

## 0. 처음 세팅 (한 번만)

### 0-1. git 설치 확인

```bash
# 버전이 나오면 설치된 것
# (Mac에서 처음 치면 "개발자 도구 설치" 창이 뜰 수 있음 → 설치 누르기)
git --version
```

### 0-2. 내 이름·이메일 등록

```bash
# 커밋에 "누가 했는지"로 기록되는 정보
# 이메일은 GitHub 계정 이메일과 같게 (그래야 GitHub에서 내 커밋으로 표시됨)
git config --global user.name "내 이름"
git config --global user.email "github계정@이메일.com"

# git pull 할 때 "어떻게 합칠지" 묻는 경고가 안 뜨게 설정 (이 가이드는 merge 방식 사용)
git config --global pull.rebase false

# 확인
git config --global --list
```

### 0-3. GitHub 로그인 (GitHub CLI)

```bash
# GitHub CLI 설치 (Homebrew 필요)
brew install gh

# 로그인 — Mac 기본 "터미널" 앱에서 실행 권장
gh auth login -h github.com -p https -w
```

- 터미널에 나온 **8자리 코드** 복사 → 엔터 → 브라우저에서 코드 붙여넣기
- **Authorize** 버튼이 회색이면 몇 초 기다리기 (GitHub 보안상 잠깐 비활성화됨)
- **Team-PickMeUp 옆 Grant 버튼**도 누르기 (안 누르면 저장소 접근 불가)
- 완료 확인:
  ```bash
  gh auth status   # "✓ Logged in to github.com" 이 나오면 성공
  ```

> 저장소가 안 보이면: GitHub 알림/이메일에서 **Team-PickMeUp 초대 수락** 했는지 확인

### 0-4. 저장소 내려받기 (clone)

```bash
# 프로젝트를 둘 폴더로 이동 (Android Studio 기본 폴더 예시)
cd ~/AndroidStudioProjects

# GitHub의 저장소를 내 컴퓨터로 복사 → pickmeup-aaos 폴더가 생김
gh repo clone Team-PickMeUp/pickmeup-aaos

cd pickmeup-aaos
git status       # "On branch main" 이 나오면 성공
```

### 0-5. Android Studio로 열기

- Android Studio → **Open** → `pickmeup-aaos` 폴더 선택
- 처음 열면 **Gradle Sync** 자동 진행 → 끝날 때까지 기다리기
  - IDE 설정 대부분은 git에 없어서 이때 자동 생성됨
- `local.properties`도 자동 생성됨 (SDK 경로 등 개인 설정, **git에 올리지 않음**)
- Pleos 에뮬레이터 실행 후 ▶ Run 으로 앱 실행 확인

---

## 1. 작업 시작 전 (매번)

**목표: 최신 `main`을 받아서, 거기서 내 작업 브랜치를 만든다.**

```bash
# 1) 지금 어느 브랜치에 있는지, 커밋 안 한 변경사항이 있는지 확인
#    "nothing to commit, working tree clean" → 깨끗한 상태
git status

# 2) 로컬 main 브랜치로 이동
#    (아직 내 컴퓨터의 main은 옛날 상태일 수 있음)
git switch main

# 3) GitHub(원격)의 최신 main을 받아서 로컬 main에 반영
#    → 다른 사람이 그동안 머지한 내용이 내 main에 들어옴
git pull

# 4) 지금 위치(최신 main)를 기준으로 새 브랜치를 만들고 그 브랜치로 이동
#    -c = create, 새로 만들 때만 붙임
git switch -c feat/pickup-target
```

### 이미 작업하던 브랜치를 이어서 할 때

```bash
# 커밋 안 한 변경사항 없는지 확인
# (있으면 브랜치 이동이 막히거나 변경사항이 따라다니니 먼저 커밋)
git status

# 로컬 main으로 이동 → 원격의 최신 main 받기
# → 바뀌는 건 main뿐, 내 작업 브랜치는 아직 그대로
git switch main
git pull

# 내 작업 브랜치로 돌아오기 (기존 브랜치라 -c 없음)
git switch feat/pickup-target

# 방금 최신화한 main의 내용을 "지금 브랜치"에 합치기
# → 바뀌는 건 내 브랜치뿐, main은 영향 없음
# → 아직 내 컴퓨터에만 반영된 상태 (push 전까지 GitHub엔 안 올라감)
git merge main
```

> 충돌(conflict)이 나면 **6. 충돌 해결** 참고

---

## 2. 작업 중

**목표: 작은 단위로 자주 커밋하고, 자주 푸시한다.**

```bash
# 어떤 파일이 바뀌었는지 목록으로 확인
git status

# 파일 안에서 정확히 어떤 줄이 바뀌었는지 확인 (q 누르면 나가짐)
git diff

# 커밋할 파일을 "담기" (staging)
# → 아직 저장된 건 아니고, 다음 커밋에 넣을 목록에 올려두는 것
git add 파일경로        # 특정 파일만 담기
git add .              # 바뀐 파일 전부 담기 (git status로 이상한 파일 없는지 먼저 확인!)

# 담아둔 파일들을 하나의 커밋으로 저장
# → 내 컴퓨터의 현재 브랜치에만 기록됨 (GitHub엔 아직 없음)
git commit -m "✨ feat: 픽업 대상자 목록 UI 추가"

# 내 커밋들을 GitHub(원격)의 같은 이름 브랜치로 올리기
# 처음 푸시: -u 옵션으로 "로컬 브랜치 ↔ 원격 브랜치" 연결
#           (원격에 브랜치가 없으면 새로 만들어짐)
git push -u origin feat/pickup-target
# 두 번째부터는 연결돼 있어서 이것만 치면 됨
git push
```

### 커밋 메시지 규칙

- 형식: `깃모지 타입: 요약` (한국어 50자 이내, 마침표 없이, "~ 추가 / ~ 수정 / ~ 삭제"로 끝냄)
- 깃모지 입력: 아래 표에서 복사하거나 Mac 이모지 창(`⌃ + ⌘ + Space`) 사용

| 깃모지 | 타입 | 언제 |
|:---:|---|---|
| ✨ | `feat` | 새 기능·화면 추가 |
| 🐛 | `fix` | 버그 수정 |
| 🚑️ | `hotfix` | `main`에서 발견된 급한 버그 수정 |
| 💄 | `design` | UI·스타일만 변경 (동작 변화 없음) |
| ♻️ | `refactor` | 동작은 그대로, 코드 구조 개선 |
| 🔥 | `remove` | 코드·파일 삭제 |
| ✅ | `test` | 테스트 추가·수정 |
| 📝 | `docs` | 문서 (README 등) |
| 🔧 | `chore` | 빌드 설정, 의존성, gitignore 등 |

### 작업 중 팁

- 하루 작업이 끝나면 완성 안 됐어도 **커밋 + 푸시** (컴퓨터 고장 대비)
- 커밋 하나에는 **한 가지 변경만**
- `git add .` 전에 꼭 `git status` 확인 → 개인 설정 파일, 빌드 결과물이 섞여 들어가는 것 방지

---

## 3. 작업 끝난 후 → PR 올리기

**목표: 최신 `main`과 합쳐서 문제없는지 확인하고 PR을 올린다.**

```bash
# 1) 커밋 안 한 변경사항 없는지 확인 (있으면 add + commit 먼저)
git status

# 2) 로컬 main으로 이동 → 원격의 최신 main 받기
#    → 내가 작업하는 동안 다른 사람이 머지한 내용이 로컬 main에 들어옴
git switch main
git pull

# 3) 내 작업 브랜치로 돌아와서, 최신 main을 내 브랜치에 합치기
#    → 바뀌는 건 내 브랜치뿐, main은 그대로
#    → 충돌이 나면 여기서 해결 (PR에서 충돌나는 것보다 훨씬 편함)
git switch feat/pickup-target
git merge main

# 4) 빌드 & 실행 확인 (Android Studio, 또는 아래 명령)
#    → 남의 코드와 합쳐진 상태에서도 잘 돌아가는지 확인
./gradlew assembleDebug testDebugUnitTest

# 5) 합쳐진 결과를 GitHub의 내 브랜치에 올리기
#    → 원격 main에는 영향 없음. main에 들어가는 건 PR 머지할 때뿐
git push
```

6. GitHub 저장소 페이지 → **Compare & pull request** 버튼 → PR 작성
   - 제목: 커밋과 같은 형식 (예: `✨ feat: 픽업 대상자 지정 화면 추가`)
   - 본문: 템플릿이 자동으로 채워짐 → 작업 내용, 테스트 방법, 체크리스트 작성
   - UI 변경 시 **에뮬레이터 스크린샷 필수**
   - 이슈가 있으면 본문에 `Closes #12`
7. 팀원 리뷰 → 수정 요청 오면 **같은 브랜치에서** 수정 → 커밋 + 푸시 (PR에 자동 반영)
8. 승인(1명 이상) 되면 **Squash and merge** 클릭
   - PR의 커밋들이 하나로 합쳐져서 `main`에 들어감 (PR 제목이 커밋 메시지가 됨)
   - GitHub의 작업 브랜치는 **자동 삭제**됨

---

## 4. 머지 후 정리

```bash
# 로컬 main으로 이동 → 방금 머지된 내 작업이 포함된 최신 main 받기
git switch main
git pull

# GitHub에서 지워진 브랜치를 내 컴퓨터 목록에서도 정리
git fetch --prune

# 다 쓴 로컬 작업 브랜치 삭제
# ⚠️ 우리는 Squash merge라서 소문자 -d 는 "아직 머지 안 됐다"며 거부함
#    → GitHub에서 PR이 Merged 상태인 것 확인 후 대문자 -D 사용
git branch -D feat/pickup-target
```

- 다음 작업은 다시 **1. 작업 시작 전**부터

---

## 5. 한눈에 보는 순서

```
[처음 한 번] git config → gh auth login → gh repo clone → Android Studio로 열기

[시작 전]  git status → git switch main → git pull → git switch -c 타입/작업-내용
[작업 중]  (코드 수정) → git status → git add → git commit → git push   ← 반복
[끝난 후]  git switch main → git pull → git switch 타입/작업-내용 → git merge main
           → 빌드 확인 → git push → GitHub에서 PR → 리뷰 → Squash and merge
[머지 후]  git switch main → git pull → git fetch --prune → git branch -D 타입/작업-내용
```

---

## 6. 충돌(conflict) 해결

`git merge main` 하다가 이런 메시지가 나오면:

```
CONFLICT (content): Merge conflict in app/src/.../SettingsScreen.kt
```

1. 해당 파일을 열면 이런 부분이 있음:
   ```
   <<<<<<< HEAD
   내 브랜치의 코드
   =======
   main의 코드
   >>>>>>> main
   ```
2. 둘 중 맞는 코드만 남기거나 합쳐서 정리, `<<<<<<<`, `=======`, `>>>>>>>` 줄은 삭제
   - Android Studio: **Git → Resolve Conflicts** 메뉴를 쓰면 화면에서 고를 수 있어 편함
3. 정리가 끝나면:
   ```bash
   git add .
   git commit        # 메시지는 자동으로 채워짐, 그대로 저장 (편집기가 열리면 :wq 입력 후 엔터)
   ```

> - 뭐가 맞는지 모르겠으면 **혼자 고르지 말고 물어보기!**
> - 그냥 취소하고 싶으면: `git merge --abort` (merge 전 상태로 돌아감)

---

## 7. 실수했을 때

| 상황 | 해결 |
|---|---|
| 파일 수정을 되돌리고 싶다 (커밋 전) | `git restore 파일경로` |
| `git add` 한 걸 취소하고 싶다 | `git restore --staged 파일경로` |
| 방금 커밋 메시지를 고치고 싶다 (푸시 전) | `git commit --amend -m "새 메시지"` |
| 방금 커밋에 파일 하나 빠뜨렸다 (푸시 전) | `git add 파일` → `git commit --amend --no-edit` |
| 다른 브랜치로 가야 하는데 작업이 덜 됐다 | `git stash` → (다른 작업) → 돌아와서 `git stash pop` |
| main에서 실수로 작업해버렸다 (커밋 전) | `git switch -c 타입/작업-내용` → 변경사항 그대로 새 브랜치로 옮겨감 |
| main에서 실수로 **커밋**해버렸다 (푸시 전) | `git switch -c 타입/작업-내용` → `git branch -f main origin/main` (커밋은 새 브랜치에 남고 main만 원래대로) |
| `git push` 했는데 rejected 가 뜬다 | 원격 브랜치에 내 컴퓨터에 없는 커밋이 있는 것 → `git pull` 후 다시 `git push` |

---

## 8. 하지 말아야 할 것

- ❌ `main`에 직접 커밋/푸시
- ❌ `git push --force` (특히 다른 사람과 같이 쓰는 브랜치에서)
- ❌ API 키, keystore(`*.jks`), `local.properties` 커밋 (`.gitignore`에 등록돼 있지만 강제로 올리지 않기)
- ❌ 이해 안 되는 에러 메시지를 무시하고 인터넷 명령어 복붙
  - 막히면 **`git status` 결과를 캡처해서 물어보기** → 대부분 여기에 답이 있음
