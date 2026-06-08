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
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java","-jar","/app/app.jar"]
