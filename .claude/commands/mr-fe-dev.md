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

커밋 내용을 분석하여 아래 템플릿의 각 섹션을 채워서 description으로 사용한다.
주석(`<!-- ... -->`)은 최종 MR description에 포함하지 않고 제거한다.
내용이 없는 섹션은 비워두거나 "X"로 표기한다. 억지로 채우지 않는다.

```
## 📄 요약 (Summary)
<!-- 이번 PR에서 어떤 작업을 했는지 간단하게 설명해주세요. -->

<br>

## 🔗 관련 이슈 (Related Issue)
<!-- 본 PR과 관련된 Jira 이슈 번호를 모두 적어주세요. -->
- Closes S14P21D108-

<br>

## ✨ 주요 변경 사항 (Key Changes)
<!-- 리스트 형식으로 작성해주세요. -->

<br>

## 📸 스크린샷 (Screenshots)
<!-- UI 변경사항이 있다면 스크린샷을 첨부해주세요. (없다면 생략) -->

<br>

## 🙏 리뷰어에게 (To the Reviewer)
<!-- 리뷰어가 특별히 신경 써서 봐주었으면 하는 부분이나, 테스트 시 참고할 사항이 있다면 알려주세요. -->
```

## 주의사항

- MR 생성 후 URL을 사용자에게 보여준다
- glab 명령어가 실패하면 에러 메시지를 보여주고 `glab auth login` 안내
- 관련 이슈 번호는 사용자에게 물어본다
