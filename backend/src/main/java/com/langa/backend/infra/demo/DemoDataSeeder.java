package com.langa.backend.infra.demo;

import com.langa.backend.common.utils.KeyGenerator;
import com.langa.backend.domain.applications.Application;
import com.langa.backend.domain.applications.repositories.ApplicationRepository;
import com.langa.backend.domain.applications.services.IngestionSizeCalculator;
import com.langa.backend.domain.applications.valueobjects.ApplicationId;
import com.langa.backend.domain.applications.valueobjects.ApplicationOwner;
import com.langa.backend.domain.applications.valueobjects.ApplicationUsage;
import com.langa.backend.domain.applications.valueobjects.LogEntry;
import com.langa.backend.domain.applications.valueobjects.MetricEntry;
import com.langa.backend.domain.applications.valueobjects.RetentionPolicy;
import com.langa.backend.domain.users.User;
import com.langa.backend.domain.users.services.UserService;
import com.langa.backend.domain.users.valueobjects.ExternalIdentity;
import com.langa.backend.infra.security.config.AuthProviderProperties;
import com.langa.backend.infra.security.localauth.LocalIdentity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Creates the demo user, the demo application and its history at startup (see {@link DemoDataProperties}).
 * Idempotent: nothing is done when the demo application already exists.
 */
@Slf4j
@Component
@ConditionalOnProperty(prefix = "application.demo", name = "enabled", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

    /** One log entry and one metric entry every this often, over the whole history. */
    static final Duration STEP = Duration.ofMinutes(2);

    private static final String[] LOGGERS = {
            "com.example.shop.orders.OrderService",
            "com.example.shop.payments.PaymentService",
            "com.example.shop.catalog.CatalogService",
            "com.example.shop.web.OrderController"};
    private static final String[] INFO_MESSAGES = {
            "Order %d created for customer %d",
            "Payment authorized for order %d (customer %d)",
            "Catalog refreshed: %d products in %d categories",
            "GET /orders served %d results for customer %d"};
    private static final String[] WARN_MESSAGES = {
            "Payment provider slow to answer for order %d (attempt %d)",
            "Stock low for product %d: %d left"};
    private static final String[] ERROR_MESSAGES = {
            "Payment declined for order %d: insufficient funds (customer %d)",
            "Order %d could not be saved, retry %d failed"};
    private static final String[][] METRICS = {
            // name, signature, uri, http method
            {"orders.create", "OrderService.createOrder(Order)", "/orders", "POST"},
            {"orders.list", "OrderService.listOrders()", "/orders", "GET"},
            {"payments.authorize", "PaymentService.authorize(Order)", "/orders", "POST"},
            {"catalog.search", "CatalogService.search(String)", "/products", "GET"}};

    private final DemoDataProperties properties;
    private final AuthProviderProperties authProperties;
    private final UserService userService;
    private final ApplicationRepository applicationRepository;
    private final IngestionSizeCalculator ingestionSizeCalculator;
    private final String ingestionBaseUrl;

    public DemoDataSeeder(DemoDataProperties properties,
                          AuthProviderProperties authProperties,
                          UserService userService,
                          ApplicationRepository applicationRepository,
                          IngestionSizeCalculator ingestionSizeCalculator,
                          @Value("${application.base-url}${application.ingestion.endpoint}") String ingestionBaseUrl) {
        this.properties = properties;
        this.authProperties = authProperties;
        this.userService = userService;
        this.applicationRepository = applicationRepository;
        this.ingestionSizeCalculator = ingestionSizeCalculator;
        this.ingestionBaseUrl = ingestionBaseUrl;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (applicationRepository.findById(properties.getAppId()).isPresent()) {
            log.info("Demo data already present, nothing to seed");
            return;
        }

        final User owner = demoUser();
        final String ingestionUri = KeyGenerator.generateIngestionUri(owner.getAccountKey(), properties.getAppKey());
        final Application application = Application.populateSecured(
                ApplicationId.of(properties.getAppId(), properties.getAppKey()),
                properties.getAppName(),
                new ApplicationOwner(owner.getAccountKey(), owner.getEmail()),
                properties.getAppSecret(),
                ingestionUri,
                new HashSet<>(),
                ApplicationUsage.empty(),
                RetentionPolicy.defaultPolicy());

        final Instant now = Instant.now();
        final Instant from = now.minus(Duration.ofHours(properties.getHistoryHours()));
        final Random random = new Random(42);
        application.createLogEntries(logs(from, now, random), ingestionSizeCalculator);
        application.createMetricEntries(metrics(from, now, random), ingestionSizeCalculator);
        applicationRepository.save(application);

        log.info("Demo data seeded: application '{}' owned by {}, ingestion URL {}/h/{}",
                properties.getAppName(), owner.getEmail(), ingestionBaseUrl, ingestionUri);
    }

    /**
     * In local authentication mode, the demo user gets the local identity it will sign in with; otherwise it is
     * created without identity and linked by email on its first sign-in.
     */
    private User demoUser() {
        final String email = LocalIdentity.normalizeEmail(properties.getEmail());
        if (LocalIdentity.PROVIDER.equals(authProperties.getProvider())) {
            return userService.provisionExternalUser(
                    new ExternalIdentity(LocalIdentity.PROVIDER, LocalIdentity.subjectFor(email), email));
        }
        return userService.findOrCreateUserByEmail(email);
    }

    static List<LogEntry> logs(Instant from, Instant to, Random random) {
        final List<LogEntry> logs = new ArrayList<>();
        for (Instant timestamp = from; timestamp.isBefore(to); timestamp = timestamp.plus(STEP)) {
            final int draw = random.nextInt(100);
            final String level = draw < 80 ? "INFO" : draw < 93 ? "WARN" : "ERROR";
            final String[] messages = switch (level) {
                case "INFO" -> INFO_MESSAGES;
                case "WARN" -> WARN_MESSAGES;
                default -> ERROR_MESSAGES;
            };
            final String message = messages[random.nextInt(messages.length)]
                    .formatted(1000 + random.nextInt(9000), 1 + random.nextInt(50));
            logs.add(new LogEntry()
                    .setMessage(message)
                    .setLevel(level)
                    .setLoggerName(LOGGERS[random.nextInt(LOGGERS.length)])
                    .setTimestamp(timestamp.plusMillis(random.nextInt((int) STEP.toMillis())))
                    .setThreadName("http-nio-8081-exec-" + (1 + random.nextInt(10)))
                    .setStackTrace("ERROR".equals(level) ? stackTrace(message) : null)
                    .setMdc(Map.of("traceId", Long.toHexString(random.nextLong()))));
        }
        return logs;
    }

    static List<MetricEntry> metrics(Instant from, Instant to, Random random) {
        final List<MetricEntry> metrics = new ArrayList<>();
        for (Instant timestamp = from; timestamp.isBefore(to); timestamp = timestamp.plus(STEP)) {
            final String[] metric = METRICS[random.nextInt(METRICS.length)];
            final boolean error = random.nextInt(100) < 5;
            metrics.add(new MetricEntry()
                    .setName(metric[0])
                    .setSignature(metric[1])
                    .setUri(metric[2])
                    .setHttpMethod(metric[3])
                    .setDurationMillis(20 + (int) Math.abs(random.nextGaussian() * 80))
                    .setStatus(error ? "ERROR" : "SUCCESS")
                    .setHttpStatus(error ? 500 : 200)
                    .setTimestamp(timestamp.plusMillis(random.nextInt((int) STEP.toMillis()))));
        }
        return metrics;
    }

    private static String stackTrace(String message) {
        return "java.lang.IllegalStateException: " + message + "\n"
                + "\tat com.example.shop.payments.PaymentService.authorize(PaymentService.java:42)\n"
                + "\tat com.example.shop.orders.OrderService.createOrder(OrderService.java:31)\n"
                + "\tat com.example.shop.web.OrderController.create(OrderController.java:24)";
    }
}
