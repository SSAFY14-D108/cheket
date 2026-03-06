# Cheket 리팩토링 사전 평가 및 실행 메모

## 문서 목적
- 추후 `MVVM + Repository + Feature-based + Domain Layer` 구조로 리팩토링할 때 기준 문서로 사용한다.
- 현재 구현 완료 이후(기능 우선 개발 후) 구조 정리를 진행하는 전략을 전제로 한다.

## 현재 상태 요약 (cheket 기준)
- Feature 단위 폴더(`features/*`)와 `ViewModel`, `Repository` 패턴이 이미 일부 적용되어 있음.
- `AppContainer` 기반 수동 DI를 사용 중.
- 데이터 계층은 `MockDataSource` 중심으로 단순 구성.
- 즉, 완전 신규 도입이 아니라 "확장/정리형 리팩토링"에 가까움.

---

## 아키텍처 평가: MVVM + Repository + Feature-based + Domain Layer

### 1) 타당성 평가

#### 기술적 타당성: 높음
- 현재 코드가 이미 ViewModel/Repository 패턴을 사용하고 있어 전환 비용이 "중간" 수준.
- 화면 단위 분리가 되어 있어 Feature 기반 레이어링과 궁합이 좋음.
- Domain Layer(UseCase, Domain Model)를 추가해도 기존 코드를 단계적으로 감쌀 수 있음.

#### 조직/협업 타당성: 높음
- 기능 단위 개발(Feature-based)로 충돌 범위가 줄고, 코드 오너십이 명확해짐.
- 신입/주니어 온보딩 시 "어디에 코드를 넣어야 하는지" 판단 기준이 명확해짐.

#### 제품/운영 타당성: 높음
- 기능 증가(예: 결제/양도/웹뷰 확장) 시 구조 붕괴 가능성을 낮춤.
- 실서비스 운영 단계에서 유지보수/결함 수정 속도 개선 기대.

### 2) 효율성 평가

#### 단기 효율(초기 2~4주): 보통
- 레이어 분리/매퍼/UseCase 추가로 파일 수 증가.
- 초기에는 개발 속도가 느려지는 체감이 있음.

#### 중장기 효율(운영 단계): 높음
- 변경 영향 범위 축소(한 기능 수정 시 타 기능 파급 감소).
- 테스트 작성 용이성 증가(ViewModel, UseCase, Repository 단위 테스트).
- 재사용성과 유지보수성이 높아져 누적 생산성이 개선됨.

#### 성능 관점: 중립
- 구조 자체가 런타임 성능을 크게 개선/악화하지는 않음.
- 다만 데이터 접근 경로가 명확해져 캐시/로컬 저장소 최적화는 쉬워짐.

---

## 장점 / 단점

### 장점
- 관심사 분리 명확(UI/상태/비즈니스/데이터 소스).
- 테스트 용이(Fake Repository, UseCase 단위 검증).
- 기능 확장 시 구조적 일관성 유지.
- 코드 리뷰 기준 통일(레이어 규칙으로 품질 관리).

### 단점
- 보일러플레이트 증가(인터페이스, 모델, 매퍼, UseCase).
- 작은 기능에도 파일 수가 늘어 초기 진입 장벽이 생김.
- 팀이 규칙을 지키지 않으면 오히려 복잡도만 증가.

---

## 결론
- 현재 cheket 상황에서는 해당 아키텍처 도입이 **타당하고, 중장기 효율성이 높다**.
- 단, "한 번에 전면 전환"보다 **기능 단위 점진 전환(Vertical Slice)**이 비용 대비 효과가 좋다.

권장 판단:
- 도입 권장: Yes
- 도입 방식: 점진 전환
- 리스크 관리: 파일럿 기능 2~3개 선적용 후 확장

---

## 나중에 실행할 리팩토링 체크리스트 (요약)

1. `domain` 레이어 신설
- `domain/model`, `domain/repository`, `domain/usecase`

2. `data` 레이어 신설
- `data/repository(impl)`, `data/datasource(remote/local/mock)`, `data/mapper`

3. `features` 정리
- `features/<feature>/presentation/screen`
- `features/<feature>/presentation/viewmodel`

4. 의존 규칙 강제
- `features -> domain`
- `data -> domain`
- `domain -> (무의존)`

5. DI 고도화(선택: Hilt)
- 수동 `AppContainer`에서 Hilt로 전환 시 `@HiltAndroidApp`, `@HiltViewModel`, Module 바인딩 적용

6. 파일럿 전환
- `auth`, `home`, `collection(WebView 포함)`부터 수직 슬라이스로 전환

7. 검증
- ViewModel/UseCase/Repository 단위 테스트
- 네비게이션/상태복원/웹뷰 세션 회귀 테스트

---

## 참고 메모 (WebView)
- WebView 화면은 `feature` 내부에 두는 것이 적절함.
- 단, URL 정책/세션/보안 규칙은 `domain/data`로 분리하는 것을 권장.
- 즉, "표현(UI)"과 "정책/비즈니스"를 분리한다.
