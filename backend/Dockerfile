FROM eclipse-temurin:21-jdk-ubi10-minimal AS build

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

COPY src src

RUN ./gradlew bootJar --no-daemon && \
    cp $(find build/libs -maxdepth 1 -name "*.jar" ! -name "*-plain.jar" | head -n 1) app.jar


FROM eclipse-temurin:21-jre-ubi10-minimal

WORKDIR /app

COPY --from=build /app/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]