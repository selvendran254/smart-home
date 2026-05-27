FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn -q clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine

WORKDIR /app
COPY --from=build /app/target/smarthome-0.0.1-SNAPSHOT.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

CMD ["java", "-jar", "app.jar"]
