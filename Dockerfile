FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
ARG APP_VERSION=0.0.0-SNAPSHOT
RUN sh ./gradlew --no-daemon "-PappVersion=${APP_VERSION}" shadowJar

FROM eclipse-temurin:25-jre
RUN apt-get update \
    && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY --from=builder /app/build/libs/TimerBot-*-all.jar /app/TimerBot.jar
USER 10001:10001
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["curl", "--fail", "--silent", "--show-error", "--noproxy", "*", "--max-time", "4", "http://127.0.0.1:8080/health"]
ENTRYPOINT ["java", "-jar", "/app/TimerBot.jar"]
