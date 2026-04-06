FROM gradle:8.12.1-jdk21 AS build
WORKDIR /home/gradle/project

COPY --chown=gradle:gradle build.gradle settings.gradle ./
COPY --chown=gradle:gradle src ./src

RUN echo '==> Iniciando build Gradle no container' \
    && gradle --version \
    && gradle --no-daemon clean bootJar \
    && echo '==> Artefatos gerados em build/libs' \
    && ls -lah build/libs \
    && cp build/libs/*.jar app.jar

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /home/gradle/project/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar", "--spring.profiles.active=postgres"]
