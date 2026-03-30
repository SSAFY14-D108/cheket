# ============================================
# Stage 1: Build (Gradle로 JAR 빌드)
# ============================================
FROM eclipse-temurin:17-jdk AS builder

WORKDIR /app

# Gradle Wrapper 및 설정 파일 먼저 복사 (캐시 활용)
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# gradlew 실행 권한 부여
RUN chmod +x gradlew

# 의존성 먼저 다운로드 (소스 변경 시 캐시 재사용)
RUN ./gradlew dependencies --no-daemon || true

# 소스 코드 복사
COPY src src
COPY config config

# JAR 빌드 (테스트 제외 - CI에서 별도로 실행)
RUN ./gradlew bootJar --no-daemon -x test -x checkstyleMain -x checkstyleTest -x spotlessCheck

# ============================================
# Stage 2: Run (JRE만으로 실행)
# ============================================
FROM eclipse-temurin:17-jre

WORKDIR /app

# 빌드된 JAR 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
