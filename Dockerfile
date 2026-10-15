FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /workspace

COPY . .

ARG MODULE
ARG APP_JAR

RUN chmod +x mvnw \
    && ./mvnw -B -ntp \
       -pl "${MODULE}" \
       -am \
       -DskipTests \
       package

FROM eclipse-temurin:21-jre

WORKDIR /app

ARG MODULE
ARG APP_JAR

COPY --from=build \
    /workspace/${MODULE}/target/${APP_JAR}-0.0.1-SNAPSHOT.jar \
    /app/app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
