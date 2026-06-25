FROM eclipse-temurin:21-jdk AS builder
WORKDIR /workspace
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
# Gradle 배포본 선다운로드 + 캐시 레이어화. CDN(services.gradle.org) 일시 5xx 대비
# 지수 백오프 재시도(최대 5회) — gradlew 기본은 1회만 시도라 504 한 번에 빌드가 죽음
RUN chmod +x gradlew && \
    n=0; until ./gradlew --version --no-daemon; do \
      n=$((n+1)); \
      if [ "$n" -ge 5 ]; then echo "gradlew distribution download failed after 5 attempts"; exit 1; fi; \
      echo "gradlew attempt $n failed, retrying in $((n*5))s..."; sleep $((n*5)); \
    done
COPY src ./src
RUN ./gradlew bootJar --no-daemon -x test

FROM eclipse-temurin:21-jre
WORKDIR /app
# OTel 자바 에이전트 — 코드 수정 없이 Spring MVC·JDBC·HTTP 호출을 자동 추적해 Tempo로 보낸다.
# 버전 고정(재현성·롤백). 트레이스 목적지·샘플링 등 OTEL_* 설정은 배포(차트 values)에서 주입.
ADD https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.29.0/opentelemetry-javaagent.jar /app/opentelemetry-javaagent.jar
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java","-javaagent:/app/opentelemetry-javaagent.jar","-jar","/app/app.jar"]
