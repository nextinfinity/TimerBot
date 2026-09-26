FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
ARG APP_VERSION=0.0.0-SNAPSHOT
RUN sh ./gradlew --no-daemon "-PappVersion=${APP_VERSION}" test shadowJar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/TimerBot-*-all.jar /app/TimerBot.jar
USER 10001:10001
HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
    CMD ["java", "-cp", "/app/TimerBot.jar", "net.nextinfinity.timerbot.HealthCheck"]
ENTRYPOINT ["java", "-jar", "/app/TimerBot.jar"]
