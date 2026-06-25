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
# 트레이스 계측은 이미지에 박지 않는다. CloudWatch Application Signals 가 EKS 파드에 ADOT 자바에이전트를
# 런타임에 자동 주입(deployment-app.yaml 의 inject-java 어노테이션). 이미지에 javaagent 를 또 박으면 이중 계측 충돌.
COPY --from=builder /workspace/build/libs/*.jar app.jar
EXPOSE 8080
ENV SPRING_PROFILES_ACTIVE=local
ENTRYPOINT ["java","-jar","/app/app.jar"]
