# Customer App Android 코딩 컨벤션 (초안)

## 1. 문서 목적
- 이 문서는 `frontend/apps/customer-app/android` 코드베이스의 현재 설정과 구현 패턴을 기준으로 코딩 컨벤션을 정리한 문서다.
- `kotlin.code.style=official`(Gradle 설정) 기반으로 작성한다.

## 2. 적용 범위
- `android/app/src/main/java/com/ssafy/cheket/**`
- `android/app/src/main/res/**`

## 3. 명시적 규칙 (설정 기반)
### 3.1 Kotlin 스타일
- `gradle.properties`의 `kotlin.code.style=official`을 따른다.
- Kotlin 공식 스타일을 벗어나는 커스텀 포맷은 도입하지 않는다.

### 3.2 빌드/언어 버전
- Java/Kotlin 타깃은 Java 11(`sourceCompatibility`, `targetCompatibility`)을 기준으로 작성한다.
- Compose 사용 프로젝트이므로 UI는 Jetpack Compose 패턴을 우선한다.

## 4. 패키지/폴더 구조 규칙
### 4.1 최상위 패키지
- 기본 패키지는 `com.ssafy.cheket`을 사용한다.

### 4.2 기능 단위 분리
- 화면/도메인은 `features/<feature>` 단위로 분리한다.
- 예: `features/auth`, `features/home`, `features/resale`

### 4.3 공통 모듈 분리
- 공통 데이터/도메인: `core/model`, `core/repository`, `core/datasource`
- 공통 UI 컴포넌트: `core/ui/component`
- 네비게이션 파라미터: `core/navigation`

## 5. 네이밍 컨벤션
### 5.1 클래스/객체/인터페이스
- 클래스/객체/인터페이스는 PascalCase를 사용한다.
- 예: `MainActivity`, `AppContainer`, `AuthRepository`

### 5.2 파일명
- 파일명은 대표 타입명과 일치시킨다.
- 예: `LoginViewModel.kt`, `AuthRepositoryImpl.kt`, `TicketCardItem.kt`

### 5.3 Compose 함수
- `@Composable` 화면 함수는 `...Screen` 접미사를 사용한다.
- 예: `LoginScreen`, `CollectionScreen`

### 5.4 ViewModel
- 화면 상태 관리 클래스는 `...ViewModel` 접미사를 사용한다.
- 화면 상태 데이터 클래스는 `...UiState` 접미사를 사용한다.

### 5.5 Repository
- 추상화는 `...Repository` 인터페이스로 정의한다.
- 구현체는 `core/repository/impl`에 `...RepositoryImpl`로 둔다.

### 5.6 상수
- 전역 라우트 상수는 `object` 내부 `UPPER_SNAKE_CASE`를 사용한다.
- 예: `Routes.LOGIN`, `Routes.MY_TICKETS`

## 6. 코드 작성 규칙
### 6.1 상태 관리
- UI 상태는 `MutableStateFlow` + `StateFlow`로 캡슐화한다.
- 외부에는 `asStateFlow()`로 read-only 상태만 노출한다.

### 6.2 ViewModel 내부 접근 제한
- 변경 가능한 상태(`_uiState`)는 `private`으로 선언한다.
- 외부 노출 상태(`uiState`)는 불변 타입으로 분리한다.

### 6.3 DI(의존성 주입) 패턴
- 앱 레벨 의존성은 `AppContainer` 인터페이스로 추상화한다.
- 실제 구현은 `RealAppContainer`에서 `lazy` 초기화한다.

### 6.4 Compose 파라미터 순서
- 콜백/의존성을 먼저 선언하고, 기본값이 있는 파라미터(예: `navController = rememberNavController()`)는 뒤에 둔다.
- trailing comma를 허용해 diff 품질을 높인다.

### 6.5 네비게이션
- 라우트 문자열은 하드코딩 분산 대신 중앙 `Routes` 객체에 모은다.
- `NavHost`에서 화면 등록 시 `composable(Routes.X)` 형식으로 참조한다.

## 7. 리소스 컨벤션
- 문자열/색상/테마는 `res/values` 및 `ui/theme`로 분리 관리한다.
- 아이콘/이미지 리소스는 Android 리소스 네이밍 규칙(소문자+언더스코어)을 따른다.

## 8. 테스트 컨벤션
- 단위 테스트는 `src/test`, 기기 테스트는 `src/androidTest`를 사용한다.
- 테스트 클래스명은 대상 클래스/기능을 드러내는 이름을 사용한다.

## 9. 금지/주의 사항
- 기능 코드에서 임의 전역 상태 공유를 피한다.
- `Repository` 인터페이스 없이 구현체에 직접 의존하지 않는다.
- 라우트 문자열/매직 스트링을 화면 코드에 중복 작성하지 않는다.

## 10. 현재 상태 메모
- 현재 프로젝트에는 `ktlint`, `detekt`, `spotless` 같은 강제 포맷터/정적분석 설정이 명시되어 있지 않다.
- 따라서 본 문서는 "실제 코드 패턴 + Kotlin official style" 기반의 운영 컨벤션이다.
- 추후 린터 도입 시 이 문서를 기준으로 룰셋을 일치시키는 것을 권장한다.
