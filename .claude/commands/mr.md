# GitLab MR 생성 (fe/dev 대상)

현재 브랜치에서 fe/dev로 GitLab Merge Request를 생성한다.

## 사전 확인

1. `glab auth status`로 로그인 상태 확인
2. `git branch --show-current`로 현재 브랜치 확인
3. `git log fe/dev..HEAD --oneline`으로 포함될 커밋 목록 확인
4. 원격에 현재 브랜치가 push되어 있지 않으면 `git push -u origin <branch>` 실행

## MR 생성

```
glab mr create \
  --target-branch fe/dev \
  --remove-source-branch \
  --title "[FE] <type>: <설명>" \
  --description "<템플릿>"
```

## MR 본문 템플릿

```
## 요약 (Summary)
## 관련 이슈 (Related Issue)
## 주요 변경 사항 (Key Changes)
## 스크린샷 (Screenshots)
## 리뷰어에게 (To the Reviewer)
```

## 주의사항

- MR 생성 후 URL을 사용자에게 보여준다
- glab 명령어가 실패하면 에러 메시지를 보여주고 `glab auth login` 안내
