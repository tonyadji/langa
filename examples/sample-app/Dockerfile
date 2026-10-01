# Sample shop instrumented with the Langa agent.
# Build context: the repository root (the agent is built from ./agent, so it always matches the backend):
#   docker build -f examples/sample-app/Dockerfile .
FROM maven:3.9.4-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Agent: dependencies first (cached while the pom does not change), then the shaded jar into the local repository
COPY agent/pom.xml agent/pom.xml
RUN mvn -B -q -f agent/pom.xml dependency:go-offline || true
COPY agent/src agent/src
RUN mvn -B -q -f agent/pom.xml install -DskipTests -Djacoco.skip=true

COPY examples/sample-app/pom.xml examples/sample-app/pom.xml
RUN mvn -B -q -f examples/sample-app/pom.xml dependency:go-offline || true
COPY examples/sample-app/src examples/sample-app/src
# Plain classpath (classes + lib/) instead of the Spring Boot fat jar: the agent starts before the application
# and needs the logging libraries (Logback, Log4j API) on the system classpath, not nested in BOOT-INF/lib
RUN mvn -B -q -f examples/sample-app/pom.xml compile dependency:copy-dependencies \
        -DincludeScope=runtime -DoutputDirectory=target/lib \
    && cp agent/target/langa-agent-*.jar /build/langa-agent.jar

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S langa && adduser -S langa -G langa
WORKDIR /app
COPY --from=build /build/langa-agent.jar langa-agent.jar
COPY --from=build /build/examples/sample-app/target/lib lib
COPY --from=build /build/examples/sample-app/target/classes classes
USER langa
EXPOSE 8081
# Required by the agent: LANGA_INGESTION_URL, LANGA_INGESTION_SECRET (and LOGGING_FRAMEWORK=logback)
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -javaagent:/app/langa-agent.jar -cp 'classes:lib/*' com.example.shop.SampleShopApplication"]
