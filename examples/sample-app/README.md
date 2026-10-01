# Langa sample app

A tiny Spring Boot shop instrumented with the [Langa agent](../../agent). It shows what an application needs
to send its logs and method timings to Langa, and feeds the "Demo Shop" application of the root
[`docker-compose.yml`](../../docker-compose.yml).

| What | Where |
|---|---|
| Timed methods (`@Monitored(name = …)`) | [`OrderService`](src/main/java/com/example/shop/orders/OrderService.java), [`PaymentService`](src/main/java/com/example/shop/payments/PaymentService.java) |
| HTTP endpoints | `GET /orders`, `POST /orders` ([`OrderController`](src/main/java/com/example/shop/orders/OrderController.java)) |
| Simulated customers | [`TrafficGenerator`](src/main/java/com/example/shop/traffic/TrafficGenerator.java) calls the endpoints every 3 s (`SAMPLE_TRAFFIC_ENABLED=false` to turn it off). About 8 % of the payments are declined and a few orders are invalid, so errors and warnings show up too |

The agent appears in three places:

- the `langa-agent` dependency in `provided` scope, for the `@Monitored` annotation;
- `-javaagent:langa-agent.jar` on the command line. The agent binds its Logback appender and times
  `@Monitored` methods with AspectJ load-time weaving. No Spring configuration is needed, and HTTP details
  (URI, method, status) are added when the method runs within a request;
- `log4j-core` in `runtime` scope. The current agent needs it at startup even for Logback (see the TODO in
  [`agent/pom.xml`](../../agent/pom.xml)).

> [!NOTE]
> The app runs from a plain classpath (`classes` + `lib/*`), not from the Spring Boot fat jar. The agent starts
> before the application and must find the logging libraries on the system classpath, while a fat jar
> keeps them nested in `BOOT-INF/lib`.

## Run with Docker

From the repository root, `docker compose up --build` starts it with the rest of the stack.
The [Dockerfile](Dockerfile) builds the agent from `../../agent`, so it always matches the backend.

## Run on your machine

```bash
# Agent (once)
cd agent && mvn install -DskipTests
cd ../examples/sample-app
mvn compile dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/lib

export LOGGING_FRAMEWORK=logback
export LANGA_INGESTION_URL=<ingestion URL of your application in the dashboard>
export LANGA_INGESTION_SECRET=<its secret>
java -javaagent:../../agent/target/langa-agent-0.0.2-M1.jar -cp 'target/classes:target/lib/*' \
  com.example.shop.SampleShopApplication
```
