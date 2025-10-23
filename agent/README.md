# 🔍 Langa Agent

[![Maven Central](https://img.shields.io/maven-central/v/com.capricedumardi/langa-agent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.capricedumardi/langa-agent)
[![Java](https://img.shields.io/badge/Java-17+-orange?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Un agent Java léger pour la collecte de logs et de métriques avec support pour Log4j2, Logback et monitoring basé sur AOP.

## 🚀 Fonctionnalités

- **📝 Collecte de logs** : Intégration transparente avec Log4j2 et Logback
- **📊 Collecte de métriques** : Monitoring des méthodes via AspectJ et Spring AOP
- **🔌 Modes d'envoi multiples** : HTTP, Kafka, ou personnalisé
- **⚡ Performance** : Buffer interne pour minimiser l'impact sur l'application
- **🔐 Sécurité** : Signature HMAC pour l'authentification
- **🎯 Non-intrusif** : Fonctionne comme un Java Agent avec instrumentation bytecode

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>com.capricedumardi</groupId>
    <artifactId>langa-agent</artifactId>
    <version>0.0.1</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.capricedumardi:langa-agent:0.0.1'
```

## 🎯 Utilisation

### Comme Java Agent

Ajoutez l'agent au démarrage de votre application :

```bash
java -javaagent:langa-agent-0.0.1.jar -jar your-application.jar
```

### Configuration

Créez un fichier `langa-agent.properties` :

```properties
# Mode d'envoi : http, kafka, noop
langa.sender.mode=http

# Configuration HTTP
langa.http.url=https://api.langa.io/logs
langa.http.apiKey=your-api-key

# Configuration Kafka (si mode=kafka)
langa.kafka.bootstrap.servers=localhost:9092
langa.kafka.topic=langa-logs

# Buffer
langa.buffer.size=1000
langa.buffer.flushInterval=5000
```

### Collecte de logs

#### Log4j2

L'agent s'intègre automatiquement via `LangaAppender` :

```xml
<!-- log4j2.xml -->
<Configuration>
    <Appenders>
        <Langa name="LangaAppender"/>
        <Console name="Console" target="SYSTEM_OUT"/>
    </Appenders>
    <Loggers>
        <Root level="info">
            <AppenderRef ref="Console"/>
            <AppenderRef ref="LangaAppender"/>
        </Root>
    </Loggers>
</Configuration>
```

#### Logback

```xml
<!-- logback.xml -->
<configuration>
    <appender name="LANGA" class="com.capricedumardi.agent.core.appenders.LangaLogbackAppender"/>
    
    <root level="INFO">
        <appender-ref ref="LANGA" />
    </root>
</configuration>
```

### Collecte de métriques

#### Avec AspectJ

Annotez vos méthodes avec `@Monitored` :

```java
import com.capricedumardi.agent.core.metrics.Monitored;

public class UserService {
    
    @Monitored
    public User createUser(String username) {
        // Votre logique métier
        return new User(username);
    }
}
```

#### Avec Spring AOP (si vous utilisez Spring)

```java
import org.springframework.stereotype.Service;
import com.capricedumardi.agent.core.metrics.Monitored;

@Service
public class OrderService {
    
    @Monitored
    public Order processOrder(Order order) {
        // Le temps d'exécution sera automatiquement mesuré
        return orderRepository.save(order);
    }
}
```

## 🏗️ Architecture

```
┌─────────────────────────────────────┐
│      Votre Application              │
│  ┌──────────┐      ┌──────────┐    │
│  │ Log4j2   │      │ Logback  │    │
│  └────┬─────┘      └────┬─────┘    │
│       │                 │           │
│       └────────┬────────┘           │
│                │                    │
│         ┌──────▼──────┐             │
│         │   Buffer    │             │
│         └──────┬──────┘             │
│                │                    │
│         ┌──────▼──────┐             │
│         │   Sender    │             │
│         └──────┬──────┘             │
└────────────────┼────────────────────┘
                 │
          ┌──────▼──────┐
          │  HTTP/Kafka │
          └──────┬──────┘
                 │
          ┌──────▼──────┐
          │ Langa Backend│
          └─────────────┘
```

## 🛠️ Développement

### Prérequis

- Java 17+
- Maven 3.8+

### Build

```bash
cd agent
mvn clean install
```

### Tests

```bash
mvn test
```

## 🤝 Contribution

Les contributions sont les bienvenues ! Consultez [CONTRIBUTING.md](../CONTRIBUTING.md) pour plus de détails.

## 📄 Licence

Ce projet est sous licence MIT. Voir [LICENSE](../LICENSE) pour plus de détails.

## 🔗 Liens utiles

- [Documentation complète](https://github.com/tonyadji/langa)
- [Exemples](https://github.com/tonyadji/langa/tree/main/examples)
- [Changelog](CHANGELOG.md)
- [Issues](https://github.com/tonyadji/langa/issues)

## 💬 Support

- 📧 Email : contact@capricedumardi.com
- 🐛 Issues : [GitHub Issues](https://github.com/tonyadji/langa/issues)

---

Développé avec ❤️ par [Caprice du Mardi](https://github.com/tonyadji)
