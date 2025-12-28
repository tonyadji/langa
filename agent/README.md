# 🔍 Langa Agent

[![Maven Central](https://img.shields.io/maven-central/v/com.capricedumardi/langa-agent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.capricedumardi/langa-agent)
[![Java](https://img.shields.io/badge/Java-17+-orange?logo=java&logoColor=white)](https://www.oracle.com/java/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

Un agent Java léger pour la collecte de logs et de métriques avec support pour Log4j2, Logback et monitoring basé sur AOP.

## 🚀 Fonctionnalités

- **📝 Collecte de logs** : Intégration transparente avec Log4j2 et Logback
- **📊 Collecte de métriques** : Monitoring des méthodes via AspectJ et Spring AOP
- **🔌 Modes d'envoi multiples** : HTTP (avec GZIP), Kafka (async), ou personnalisé
- **⚡ Performance** : Buffer intelligent avec retry et batching configurable
- **🔐 Sécurité** : Signature HMAC pour l'authentification
- **🎯 Non-intrusif** : Fonctionne comme un Java Agent avec instrumentation bytecode
- **🔄 Circuit Breaker** : Protection contre les backends défaillants
- **⚙️ Configuration flexible** : Multi-source (fichier, env vars, system props)
- **📈 Monitoring** : Support JMX et Spring Boot Actuator
- **🛡️ Résilience** : Retry automatique avec exponential backoff

## 📦 Installation

### Maven

```xml
<dependency>
    <groupId>com.capricedumardi</groupId>
    <artifactId>langa-agent</artifactId>
    <version>0.0.1-M1</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.capricedumardi:langa-agent:0.0.1-M1'
```

## 🎯 Utilisation

### Comme Java Agent

Ajoutez l'agent au démarrage de votre application :

```bash
java -javaagent:langa-agent-0.0.1-M1.jar -jar your-application.jar
```

### Nouvelles Fonctionnalités (v0.0.1-M1)

#### 🔄 Circuit Breaker
Protection automatique contre les backends défaillants :
- **CLOSED** : Fonctionnement normal
- **OPEN** : Trop d'échecs, requêtes rejetées immédiatement  
- **HALF_OPEN** : Test de récupération après timeout

#### 📊 Monitoring JMX/Actuator
- **JMX MBeans** : Accès aux métriques et configuration runtime
- **Spring Boot Actuator** : Endpoints `/actuator/langa-metrics` et `/actuator/langa-control`
- **Statistiques en temps réel** : Buffer stats, circuit breaker state, success/failure rates

#### 🗜️ Compression GZIP
Compression automatique des payloads HTTP > 1KB (réduction de 90% de la bande passante)

#### 🔁 Retry Intelligent  
Retry automatique avec exponential backoff configurable

#### ⚙️ Configuration Dynamique
Mise à jour de la configuration sans redémarrage via JMX/Actuator

### Configuration

L'agent utilise une configuration multi-source avec priorité :
1. System Properties (`-Dlanga.xxx=value`)
2. Variables d'environnement (`LANGA_XXX=value`)  
3. Fichier de configuration (`langa-agent.properties`)
4. Valeurs par défaut

#### Configuration minimale requise

```bash
# Framework de logging
export LOGGING_FRAMEWORK=logback  # ou log4j2

# URL d'ingestion  
export LANGA_INGESTION_URL=https://api.langa.io/api/ingestion/h/base64creds

# Clé secrète
export LANGA_INGESTION_SECRET=your-secret-key
```

#### Configuration avancée (optionnelle)

Créez un fichier `langa-agent.properties` :

```properties
# ========================================
# Configuration Buffer
# ========================================
langa.buffer.batch.size=100
langa.buffer.flush.interval.seconds=10
langa.buffer.main.queue.capacity=20000
langa.buffer.retry.queue.capacity=10000

# ========================================
# Configuration HTTP
# ========================================
langa.http.max.connections.total=200
langa.http.compression.threshold.bytes=1024
langa.http.max.retry.attempts=3

# ========================================
# Configuration Kafka (si Kafka)
# ========================================
langa.kafka.async.send=true
langa.kafka.compression.type=snappy
langa.kafka.batch.size.bytes=16384

# ========================================
# Circuit Breaker
# ========================================
langa.circuit.breaker.failure.threshold=5
langa.circuit.breaker.open.duration.millis=30000

# ========================================
# Debug
# ========================================
langa.debug.mode=false
```

📖 **Voir le [Guide de Configuration Complet](src/main/resources/configuration-guide.md) pour tous les paramètres disponibles**

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

- [Documentation complète](https://github.com/langa-org/langa)
- [Exemples](https://github.com/langa-org/langa/tree/main/examples)
- [Changelog](CHANGELOG.md)
- [Issues](https://github.com/langa-org/langa/issues)

## 💬 Support

- 📧 Email : contact@capricedumardi.com
- 🐛 Issues : [GitHub Issues](https://github.com/langa-org/langa/issues)

---

Développé avec ❤️ par [Caprice du Mardi](https://github.com/langa-org)
