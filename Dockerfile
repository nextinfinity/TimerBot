FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY . .
RUN sh ./gradlew --no-daemon shadowJar

FROM eclipse-temurin:25-jre
WORKDIR /app
COPY --from=builder /app/build/libs/TimerBot-*-all.jar /app/TimerBot.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/TimerBot.jar"]
