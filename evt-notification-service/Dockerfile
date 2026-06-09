FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY evt-notification-service ./evt-notification-service
RUN mvn -f evt-notification-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/evt-notification-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
