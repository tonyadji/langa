# Changelog

Toutes les modifications notables du projet Langa Agent seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère au [Versioning Sémantique](https://semver.org/lang/fr/).

## [Unreleased]

### À venir
- Support pour plus de senders (gRPC, AMQP)
- Métriques JVM automatiques
- Dashboard de configuration

---

## [0.0.1] - 2025-10-23

### 🎉 Première release !

#### Ajouté
- **Collecte de logs**
  - Appender Log4j2 (`LangaAppender`)
  - Appender Logback (`LangaLogbackAppender`)
  - Support des niveaux de log (TRACE, DEBUG, INFO, WARN, ERROR, FATAL)
  - Capture des exceptions avec stack traces

- **Collecte de métriques**
  - Annotation `@Monitored` pour le monitoring de méthodes
  - Support AspectJ pour instrumentation bytecode
  - Support Spring AOP pour applications Spring
  - Collecte automatique des temps d'exécution
  - Capture des exceptions dans les métriques

- **Senders**
  - `HttpSenderService` : Envoi via HTTP/HTTPS
  - `KafkaSenderService` : Envoi via Apache Kafka
  - `NoOpSenderService` : Mode désactivé pour tests
  - `SenderServiceFactory` : Factory pattern pour configuration

- **Buffering**
  - `GenericBuffer` : Buffer circulaire thread-safe
  - Configuration de la taille du buffer
  - Flush automatique périodique
  - Flush manuel disponible

- **Sécurité**
  - Authentification HMAC-SHA256
  - Support des API keys
  - `CredentialsHelper` pour la gestion des credentials

- **Modèle de données**
  - `LogRequestDto` : DTO pour les logs
  - `MetricRequestDto` : DTO pour les métriques
  - `SendableRequestType` : Enum pour typage

- **Configuration**
  - Support des fichiers properties
  - Configuration via variables d'environnement
  - Configuration programmatique

- **Performance**
  - Traitement asynchrone des logs
  - Buffer interne pour minimiser l'impact
  - Thread dédié pour le flush

#### Dépendances
- Java 17+
- Log4j2 2.25.1
- Logback 1.5.18
- AspectJ 1.9.24
- Spring AOP 6.2.11 (optionnel)
- Spring Context 6.2.11 (optionnel)
- Apache Kafka Clients 3.8.0
- OkHttp 5.1.0
- Apache HttpClient 4.5.14
- Gson 2.13.1

---

## Types de changements

- `Ajouté` : nouvelles fonctionnalités
- `Modifié` : modifications de fonctionnalités existantes
- `Déprécié` : fonctionnalités qui seront supprimées
- `Supprimé` : fonctionnalités supprimées
- `Corrigé` : corrections de bugs
- `Sécurité` : corrections de vulnérabilités

---

[Unreleased]: https://github.com/tonyadji/langa/compare/v0.0.1...HEAD
[0.0.1]: https://github.com/tonyadji/langa/releases/tag/v0.0.1
