# Langa Backend Documentation

**Complete documentation following the Diátaxis framework**

Welcome to the comprehensive documentation for Langa Backend - a centralized observability and application monitoring platform built with Spring Boot 3.5.5 and Java 21.

---

## 📚 Documentation Structure

This documentation follows the [Diátaxis framework](https://diataxis.fr/), providing four distinct types of documentation to serve different user needs:

### 1. [📋 Specification](./01-SPECIFICATION.md)
**For: All developers seeking complete technical specifications**

Start here to understand the complete system architecture, API contracts, data models, and business rules. This document is based on the actual implementation and provides authoritative reference for all technical decisions.

**Contents:**
- System architecture and technology stack
- Complete domain model (Applications, Users, Teams, Logs, Metrics)
- Full REST API specification with request/response formats
- Security model (JWT authentication, HMAC ingestion, authorization)
- Event-driven architecture
- Configuration reference
- Business rules and constraints
- Error codes and handling
- Non-functional requirements (performance, scalability, reliability)

**Use when:** You need to understand what the system does, how it's structured, or need authoritative technical details.

---

### 2. [🎓 Tutorial: Getting Started with Langa](./02-TUTORIAL.md)
**For: Dashboard developers (beginner level)**  
**Duration: 30-45 minutes**

A hands-on, learning-oriented guide that walks you through building your first Langa-enabled application and dashboard from scratch.

**What you'll build:**
- ✅ Set up Langa Backend locally
- ✅ Register a user account and authenticate
- ✅ Create your first monitored application
- ✅ Send logs via HTTP ingestion
- ✅ Query logs through the API
- ✅ Build a React dashboard to visualize logs

**Prerequisites:**
- Java 21, MongoDB, Node.js 18+
- Basic knowledge of REST APIs and React

**Use when:** You're new to Langa and want to learn by doing.

---

### 3. [🛠️ How-To Guides](./03-HOW-TO.md)
**For: Developers and DevOps engineers**  
**Purpose: Step-by-step recipes for specific tasks**

Goal-oriented guides that show you how to accomplish specific tasks. Each guide is a self-contained recipe you can follow.

**Topics covered:**

#### Application Management
- How to create an application
- How to share an application with a user or team
- How to revoke application access
- How to check application usage

#### Ingestion Implementation
- How to implement HTTP ingestion in Java
- How to implement HTTP ingestion in Python
- How to implement Kafka ingestion
- How to batch logs for better performance

#### Team Collaboration
- How to create a team
- How to invite someone to a team
- How to accept a team invitation
- How to manage team members

#### Querying and Filtering
- How to filter logs by level, logger name, time range
- How to paginate results
- How to combine multiple filters
- How to query metrics

#### Security and Authentication
- How to refresh an expired token
- How to handle authentication in a dashboard
- How to secure ingestion credentials

#### Deployment
- How to deploy with Docker
- How to deploy to Kubernetes
- How to configure for production

#### Monitoring and Maintenance
- How to monitor application health
- How to view application metrics
- How to enable debug logging
- How to backup MongoDB data
- How to scale horizontally

**Use when:** You have a specific task to accomplish and need step-by-step instructions.

---

### 4. [📖 Reference Documentation](./04-REFERENCE.md)
**For: All developers needing quick lookups**  
**Purpose: Information-oriented technical reference**

Complete technical reference for all APIs, data models, configuration options, and error codes. Organized for quick lookup.

**Contents:**

- **REST API Reference** - Every endpoint with parameters, request/response formats, error codes
  - Authentication endpoints (register, login, refresh)
  - Application endpoints (create, list, query logs/metrics, share, revoke)
  - Ingestion endpoint (HTTP ingestion with signature validation)
  - Team endpoints (create, invite, accept invitations)
  - User endpoints (profile, setup)
  - Actuator endpoints (health, metrics)

- **Data Models** - Complete field definitions
  - Domain entities (Application, User, Team, LogEntry, MetricEntry, etc.)
  - Value objects (ShareWith, TeamMember, TeamInvitation)
  - Enumerations (TeamRole, InvitationStatus, SharedWithProfile)

- **Configuration Reference** - All environment variables
  - Server configuration
  - Database configuration
  - Security configuration (JWT, CORS)
  - Kafka configuration
  - Email configuration
  - Logging configuration

- **Error Codes** - All error codes with HTTP status and descriptions

- **Event Types** - Domain events and their payloads

- **MongoDB Collections** - Collection names and purposes

- **HTTP Headers** - Required headers for API and ingestion

**Use when:** You need to quickly look up an API endpoint, configuration option, or data structure.

---

### 5. [💡 Explanation: Understanding Langa](./05-EXPLANATION.md)
**For: All technical stakeholders**  
**Purpose: Deep understanding of architecture and design**

Understanding-oriented documentation that explains the "why" behind design decisions, architectural patterns, and core concepts.

**Topics explained:**

#### Architecture Overview
- What is Langa Backend and why does it exist?
- System context and ecosystem
- Core purpose and problems solved

#### Clean Architecture Implementation
- Why Clean Architecture?
- Layer structure and dependency rules
- Domain, Application, Infrastructure layers
- Benefits and trade-offs

#### Security Model
- JWT authentication (why JWT over sessions?)
- Authorization model (owner, shared user, team member)
- Ingestion security (HMAC signatures)
- CORS configuration

#### Event-Driven Architecture
- Why event-driven?
- Architecture and flow
- Outbox pattern for reliability
- Event listeners and side effects

#### Multi-Tenancy Model
- What is multi-tenancy?
- Implementation strategy (shared database with discriminators)
- Data isolation and query filtering
- Sharing model (user-to-user, user-to-team)

#### Ingestion Pipeline
- HTTP vs. Kafka ingestion
- Data processing flow
- Performance optimizations
- When to use each method

#### Team Collaboration Model
- Purpose and workflow
- Team structure and roles
- Invitation lifecycle
- Permission matrix

#### Performance and Scalability
- Current performance metrics
- Bottlenecks and solutions
- Vertical vs. horizontal scaling
- Database scaling (replica sets, sharding)
- Caching strategy

#### Design Decisions
- Why MongoDB instead of SQL?
- Why JWT instead of sessions?
- Why event-driven architecture?
- Why Clean Architecture?

#### Trade-offs and Limitations
- Known limitations (no deletes, basic queries, no real-time)
- Design trade-offs and when to reconsider

**Use when:** You want to understand the reasoning behind architectural decisions or need to evaluate if Langa fits your use case.

---

## 🚀 Quick Start Guide

### For Dashboard Developers
1. Read: [Tutorial](./02-TUTORIAL.md) - Build your first dashboard (30-45 min)
2. Reference: [API Reference](./04-REFERENCE.md) - Look up specific endpoints
3. Explore: [How-To Guides](./03-HOW-TO.md) - Implement specific features

### For Backend Developers
1. Read: [Specification](./01-SPECIFICATION.md) - Understand the complete system
2. Read: [Explanation](./05-EXPLANATION.md) - Understand architectural decisions
3. Reference: [How-To Guides](./03-HOW-TO.md) - Implement features or deploy

### For DevOps Engineers
1. Read: [Specification](./01-SPECIFICATION.md) - Section 12 (Deployment)
2. Reference: [How-To Guides](./03-HOW-TO.md) - Deployment section
3. Reference: [Configuration Reference](./04-REFERENCE.md) - All environment variables

### For Architects/Technical Leads
1. Read: [Explanation](./05-EXPLANATION.md) - Complete architectural understanding
2. Read: [Specification](./01-SPECIFICATION.md) - Technical specifications
3. Evaluate: Trade-offs section in Explanation

---

## 🎯 Documentation Goals

This documentation is designed to enable you to:

✅ **Deploy Langa Backend** - Production-ready deployment on Docker or Kubernetes  
✅ **Integrate Applications** - Send logs and metrics from your applications  
✅ **Build Dashboards** - Create custom visualization dashboards using the API  
✅ **Manage Teams** - Set up team collaboration and access control  
✅ **Understand Architecture** - Make informed decisions about using/extending Langa  
✅ **Troubleshoot Issues** - Debug problems with comprehensive reference material  

---

## 📦 Project Information

- **Version:** 0.0.1-SNAPSHOT
- **Language:** Java 21
- **Framework:** Spring Boot 3.5.5
- **Database:** MongoDB
- **Messaging:** Apache Kafka (optional)
- **Architecture:** Clean Architecture / Hexagonal Architecture
- **Repository:** https://github.com/tonyadji/langa

---

## 🤝 Contributing

Found an error in the documentation? Want to add examples or clarifications?

1. Open an issue describing the documentation improvement
2. Submit a pull request with your changes
3. Ensure changes follow the Diátaxis principles

---

## 📞 Support

- **Issues:** [GitHub Issues](https://github.com/tonyadji/langa/issues)
- **Email:** motodigo.appvenger@gmail.com

---

## 📝 Documentation Maintenance

**Last Updated:** December 28, 2025  
**Maintained By:** Langa Development Team  
**Review Cycle:** Quarterly or on major releases  

---

## 🗺️ Navigation Map

```
Langa Backend Documentation
│
├── 01-SPECIFICATION.md         ← What the system is and does
│   ├── Architecture
│   ├── Domain Model
│   ├── API Specification
│   ├── Security Model
│   ├── Configuration
│   └── Business Rules
│
├── 02-TUTORIAL.md              ← Learn by building
│   ├── Setup Backend
│   ├── Create Account
│   ├── Create Application
│   ├── Send Logs
│   ├── Query Logs
│   └── Build Dashboard
│
├── 03-HOW-TO.md                ← Solve specific problems
│   ├── Application Management
│   ├── Ingestion Implementation
│   ├── Team Collaboration
│   ├── Querying and Filtering
│   ├── Security
│   ├── Deployment
│   └── Monitoring
│
├── 04-REFERENCE.md             ← Look up details
│   ├── REST API Reference
│   ├── Data Models
│   ├── Configuration
│   ├── Error Codes
│   └── Events
│
└── 05-EXPLANATION.md           ← Understand the why
    ├── Architecture Overview
    ├── Clean Architecture
    ├── Security Model
    ├── Event-Driven Architecture
    ├── Multi-Tenancy
    ├── Ingestion Pipeline
    ├── Performance
    └── Design Decisions
```

---

## 🎓 Learning Paths

### Path 1: Dashboard Developer (Beginner)
```
Tutorial → API Reference → How-To: Querying and Filtering → Build!
```

### Path 2: Integration Engineer
```
Specification: Ingestion → How-To: Ingestion Implementation → Reference: API → Integrate!
```

### Path 3: Platform Engineer / DevOps
```
Specification: Architecture → How-To: Deployment → Reference: Configuration → Deploy!
```

### Path 4: Backend Developer / Contributor
```
Explanation: Complete → Specification: Complete → How-To: Development → Contribute!
```

---

**Happy Building! 🚀**

For questions or feedback, please open an issue or discussion in the repository.
