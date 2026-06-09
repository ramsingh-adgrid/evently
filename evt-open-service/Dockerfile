FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY evt-grpc-contracts ./evt-grpc-contracts
RUN mvn -f evt-grpc-contracts/pom.xml clean install -DskipTests

COPY evt-commom-util ./evt-commom-util
RUN mvn -f evt-commom-util/pom.xml clean install -DskipTests

COPY evt-open-service ./evt-open-service
RUN mvn -f evt-open-service/pom.xml package -DskipTests

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/evt-open-service/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
