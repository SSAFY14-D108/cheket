# Cheket - React Native App

## 로컬 실행 방법

### 사전 요구사항
- Node.js >= 22.11.0
- Android Studio (JDK 내장됨)
- Android SDK
- Android 에뮬레이터 (AVD 설정 필요)

> ⚠️ **Windows 사용자**: `npm start`, `npm run android`는 반드시 **CMD 또는 PowerShell**에서 실행하세요. Git Bash에서는 동작하지 않습니다.

---

### 1. 의존성 설치
```bash
npm install
```

---

### 2. 환경변수 설정 (최초 1회, CMD에서 실행 후 터미널 재시작)

**ANDROID_HOME**
```cmd
setx ANDROID_HOME "C:\Users\{본인계정}\AppData\Local\Android\Sdk"
setx PATH "%PATH%;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\emulator"
```

**JAVA_HOME** (Android Studio 내장 JDK 사용)
```cmd
setx JAVA_HOME "C:\Program Files\Android\Android Studio\jbr"
```

> ⚠️ 설정 후 반드시 터미널을 **껐다 다시 열어야** 적용돼요.

확인:
```cmd
echo %ANDROID_HOME%
echo %JAVA_HOME%
java -version
```

---

### 3. Windows 260자 경로 제한 해결 (최초 1회, Windows만 해당)

> ⚠️ 프로젝트 경로가 깊어 빌드 시 **260자 경로 제한** 오류가 발생할 수 있습니다.
> `android/app/build.gradle`에 CMake 빌드 출력 경로를 짧게 설정하여 해결합니다.

`android/app/build.gradle`의 `android {}` 블록 안에 아래 내용을 추가했습니다:

```gradle
android {
    // ... 기존 설정 ...

    externalNativeBuild {
        cmake {
            buildStagingDirectory "C:/tmp/cheket-cxx"
        }
    }
}
```

> 이미 이 설정은 저장소에 반영되어 있으므로 **별도 작업 없이 바로 빌드 가능**합니다.

---

### 4. `android/local.properties` 파일 생성

> ⚠️ 이 파일은 .gitignore에 포함되어 있어 **각자 직접 생성**해야 합니다.

**Windows**
```
sdk.dir=C\:\\Users\\{본인계정}\\AppData\\Local\\Android\\Sdk
```

**Mac**
```
sdk.dir=/Users/{본인계정}/Library/Android/sdk
```

---

### 5. 에뮬레이터 실행
Android Studio에서 AVD Manager를 열어 에뮬레이터를 먼저 실행하세요.
터미널에서 실행 시: emulator -avd <에뮬레이터 이름>
---

### 6. 앱 실행 (터미널 2개)

**터미널 1 - Metro 번들러**
```bash
npm start
```

**터미널 2 - Android 빌드 및 실행**
```bash
npm run android
또는
npx react-native run-android
```

---

## 개발 팁

- 앱 리로드: 에뮬레이터에서 `R` 키 두 번 또는 `Ctrl + M` → Reload
- Fast Refresh: 파일 저장 시 자동으로 앱에 반영됨

---

## 프로젝트 구조

```
cheket/
├── android/          # Android 네이티브 코드
├── ios/              # iOS 네이티브 코드
├── src/              # 소스 코드
├── App.tsx           # 앱 진입점
└── index.js          # 등록 파일
```
