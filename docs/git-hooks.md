# HannahPay Git Hooks

이 저장소는 글로벌 Git 훅 대신 저장소 내부 `.githooks` 디렉토리를 사용합니다.

## 설치

```sh
git config core.hooksPath .githooks
```

또는 저장소 루트에서:

```sh
sh scripts/setup-git-hooks.sh
```

## 적용 규칙

- 브랜치 이름: `main`, `feature/*`, `hotfix/*`
- 커밋 메시지: `[type] message`
- 허용 type: `feature`, `fix`, `refactor`, `test`, `docs`, `chore`, `build`, `ci`

## 훅 동작

- `pre-commit`: 브랜치 이름 검사
- `commit-msg`: 커밋 메시지 형식 검사
- `pre-push`: 브랜치 이름 검사
