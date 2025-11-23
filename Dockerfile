FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY backend/pom.xml .
COPY backend/src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar langa-backend.jar
ENTRYPOINT ["java", "-jar", "langa-backend.jar"]