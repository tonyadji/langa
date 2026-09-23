FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /app
# Dependencies first, in their own layer (cached while the pom does not change)
COPY backend/pom.xml .
RUN mvn -B -q dependency:go-offline
COPY backend/src ./src
# Unit tests run during the image build; pass --build-arg SKIP_TESTS=true to skip them
ARG SKIP_TESTS=false
RUN mvn -B clean package -DskipTests=${SKIP_TESTS}

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S langa && adduser -S langa -G langa
WORKDIR /app
COPY --from=build /app/target/*.jar langa-backend.jar
USER langa
EXPOSE 8080
# The JVM sizes its heap from the container memory limit
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
HEALTHCHECK --interval=30s --timeout=5s --start-period=90s --retries=3 \
    CMD wget -qO- "http://localhost:${SERVER_PORT:-8080}/actuator/health/liveness" > /dev/null || exit 1
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar langa-backend.jar"]
