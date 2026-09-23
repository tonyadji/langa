# 🚀 Monitoring Platform

![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-blue?logo=react&logoColor=white)
![MongoDB](https://img.shields.io/badge/Database-MongoDB-brightgreen?logo=mongodb&logoColor=white)
[![Maven Central](https://img.shields.io/maven-central/v/com.capricedumardi/langa-agent.svg?label=Maven%20Central)](https://central.sonatype.com/artifact/com.capricedumardi/langa-agent)
![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)
![Status](https://img.shields.io/badge/Status-Work%20in%20Progress-lightgrey)

---

This repository contains a full-stack monitoring platform composed of three main components:

- **Agent** (`agent/`): A lightweight Java/Maven application that collects logs and metrics.
- **Backend** (`backend/`): A Spring Boot REST API that stores and exposes application logs.
- **Frontend** (`frontend/`): A React application that provides a dashboard to visualize logs.

---

## 🛠️ Tech Stack

- **Agent**: Java 17, Maven  
- **Backend**: Spring Boot 3, Spring Data, REST API, MongoDB/PostgreSQL (configurable)  
- **Frontend**: React, Vite/CRA, Chakra UI, Axios  

---

## 📂 Repository Structure

.
├── agent/
├── backend/
├── frontend/ 
└── README.md

---

## 🌟 Features

- Collect application logs via the **agent**  
- Store and expose logs through the **backend** REST API  
- Visualize logs in a clean **React dashboard**  
- Authentication with Microsoft Entra External ID (OAuth2 / OpenID Connect)  
- Pagination and filtering of logs  

---

## 🚀 Quick Start

### 1️⃣ Clone the repo
```bash
git clone https://github.com/langa-org/langa.git
cd <langa>---

## 🌟 Features

- Collect application logs via the **agent**  
- Store and expose logs through the **backend** REST API  
- Visualize logs in a clean **React dashboard**  
- Authentication with Microsoft Entra External ID (OAuth2 / OpenID Connect)  
- Pagination and filtering of logs  

---

## 🚀 Quick Start

### 1️⃣ Clone the repo
```bash
git clone https://github.com/langa-org/langa.git
cd <langa>

2️⃣ Start each service

Backend
cd backend
./mvnw spring-boot:run

Frontend
cd frontend
npm install
npm start

sign up (Microsoft Entra External ID) and create an application, so that you can configure the agent to send logs

📡 API Overview
Some useful endpoints from the backend:

GET /api/applications → list applications

GET /api/applications/{id}/logs?page=0&size=20 → get paginated logs for an application

GET /api/users/me → current user (requires an Entra ID access token)

## 🚢 Backend deployment

The root `Dockerfile` builds the backend image: unit tests run during the build
(`--build-arg SKIP_TESTS=true` to skip them), the application runs as a non-root user and the image
declares a `HEALTHCHECK` on the liveness probe.

```bash
docker build -t langa-backend .
docker run --env-file backend/.env -p 8080:8080 langa-backend
```

Checklist:

1. Fill the environment from [`backend/.env.example`](backend/.env.example) (variables without value are required).
2. Identity provider: production redirect URIs on the SPA app registration, `AUTH_*` variables on the backend,
   `VITE_*` variables when building the frontend (see *Authentication* below).
3. `CORS_ALLOWED_ORIGINS` and `FRONT_URL` set to the production frontend URL.
4. `SECURITY_UNSECURED_ENDPOINTS` limited to `/api/ingestion/**,/api/team-invitations/*/public`.
5. Swagger disabled (`SPRINGDOC_ENABLED` unset or `false`) and `application.security.dev-token` not enabled.
6. Probes: liveness `/actuator/health/liveness`, readiness `/actuator/health/readiness` (public, without
   details unless `MANAGEMENT_HEALTH_SHOW_DETAILS` is set).
7. Ingestion limits reviewed for the expected traffic: `INGESTION_MAX_PAYLOAD_BYTES` (default 5 MB) and
   `INGESTION_REQUESTS_PER_MINUTE` (default 1200 per application key and per backend instance, `0` to disable).
   Over the limits the endpoint answers `413` / `429` (with `Retry-After`).
8. E-mails: an SMTP server able to send the expected volume (a transactional provider rather than Gmail).

## 🔐 Authentication (OpenID Connect provider)

Users and tokens are managed by an external identity provider: Microsoft Entra External ID today, any OpenID
Connect provider (Amazon Cognito, Keycloak, Auth0...) tomorrow. The frontend signs users in (authorization
code + PKCE) and the backend only validates the access tokens. On the first request of a user, the backend
creates the Langa user, or links an existing one having the same email (account key and data are kept).

The provider is isolated from the business code:

- **Backend**: generic `application.security.auth` settings (`AUTH_*` variables below) and the
  `ExternalIdentityResolver` port (`infra/security/identity`), which reads the user identity either from the
  access token claims or from the OIDC UserInfo endpoint.
- **Frontend**: the `AuthClient` interface (`src/features/auth/providers`), selected with `VITE_AUTH_PROVIDER`.
  Pages and components only use `useAuth()`.

### Backend settings

| Variable | Default | Entra External ID | Amazon Cognito |
|---|---|---|---|
| `AUTH_PROVIDER` | `entra` | `entra` | `cognito` |
| `AUTH_ISSUER_URI` | | `https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0` | `https://cognito-idp.<region>.amazonaws.com/<user-pool-id>` |
| `AUTH_JWK_SET_URI` | | `https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/discovery/v2.0/keys` | `https://cognito-idp.<region>.amazonaws.com/<user-pool-id>/.well-known/jwks.json` |
| `AUTH_AUDIENCE_CLAIM` | `aud` | `aud` | `client_id` |
| `AUTH_AUDIENCES` | | `<langa-api client id>,api://<langa-api client id>` | `<app client id>` |
| `AUTH_SCOPE_CLAIM` | `scp` | `scp` | `scope` |
| `AUTH_REQUIRED_SCOPE` | `access_as_user` | `access_as_user` | `<resource server id>/<scope>` (e.g. `langa-api/access`) |
| `AUTH_REQUIRED_CLAIMS` | | | `token_use=access` |
| `AUTH_SUBJECT_CLAIM` | `oid` | `oid` | `sub` |
| `AUTH_EMAIL_SOURCE` | `claim` | `claim` | `userinfo` (Cognito access tokens have no email) |
| `AUTH_EMAIL_CLAIM` | `email` | `email` | `email` |
| `AUTH_USERINFO_URI` | | | `https://<cognito-domain>/oauth2/userInfo` |

The UserInfo endpoint is only called on the first sign-in of a user; afterwards users are found by
provider + subject. Users are stored with their provider, so switching providers means users sign up again
with the same email and are linked to their existing Langa account.

### Microsoft Entra External ID setup

1. App registration **langa-api**: expose the scope `access_as_user`, set `accessTokenAcceptedVersion` to `2`
   in the manifest and add the optional claim `email` to the access token.
2. App registration **langa-spa**: platform *Single-page application* with the frontend URLs as redirect URIs,
   delegated permission `access_as_user` on langa-api (admin consent granted).
3. User flow *Sign up and sign in* (email + password or one-time code) linked to langa-spa.

Frontend settings (build time):

| Variable | Example |
|---|---|
| `VITE_AUTH_PROVIDER` | `entra` |
| `VITE_ENTRA_CLIENT_ID` | `<langa-spa client id>` |
| `VITE_ENTRA_AUTHORITY` | `https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/` (the tenant id is required: MSAL checks the issuer) |
| `VITE_ENTRA_API_SCOPE` | `api://<langa-api client id>/access_as_user` |

### Amazon Cognito (example, not implemented in the frontend yet)

1. User pool with email as sign-in attribute and self sign-up enabled, a domain (Hosted UI / managed login).
2. Resource server `langa-api` with a custom scope `access`.
3. Public app client (no secret), authorization code grant, scopes `openid email langa-api/access`,
   callback and sign-out URLs of the frontend.
4. Backend: the Cognito column above.
5. Frontend: implement `AuthClient` (e.g. `oidcAuthClient.ts` with `oidc-client-ts`, authority
   `https://cognito-idp.<region>.amazonaws.com/<user-pool-id>`, scope `openid email langa-api/access`;
   `register` redirects to `https://<cognito-domain>/signup`, `logout` to `https://<cognito-domain>/logout`),
   register it in `src/features/auth/providers/index.ts` under `cognito` and set `VITE_AUTH_PROVIDER=cognito`.

### Getting an access token without the frontend (development only)

For API tests (Postman, curl...), the backend can return a real access token for a user, using the
**native authentication API** of Entra External ID with an email one-time code. It is **disabled by default**
and must never be enabled in production.

Entra setup: an app registration **langa-test-client** with *Allow public client flows* and
*Enable native authentication* set to **Yes**, the delegated permission `access_as_user` on langa-api
(admin consent granted), linked to the user flow.

Backend settings (`application-local.yml`):

| Property | Example |
|---|---|
| `application.security.dev-token.enabled` | `true` (default `false`: the endpoint does not exist) |
| `application.security.dev-token.client-id` | `<langa-test-client client id>` |
| `application.security.dev-token.native-auth-uri` | `https://<tenant-subdomain>.ciamlogin.com/<tenant-id>` |
| `application.security.dev-token.scope` | `api://<langa-api client id>/access_as_user` |

Usage:

```bash
# 1. A one-time code is sent to the user's email
curl -X POST http://localhost:8080/api/dev/token/start -H "Content-Type: application/json" \
     -d '{"username":"user@example.com"}'
# → {"continuationToken":"…","codeSentTo":"u***@example.com","codeLength":8,"signUp":false}

# To create the user in the identity provider if it does not exist (an existing user just signs in);
# displayName is optional (default: the part of the email before '@')
curl -X POST http://localhost:8080/api/dev/token/start -H "Content-Type: application/json" \
     -d '{"username":"new@example.com","signUp":true,"displayName":"New User"}'
# → {"continuationToken":"…","codeSentTo":"n***@example.com","codeLength":8,"signUp":true}

# 2. Exchange the code for an access token (within a few minutes)
curl -X POST http://localhost:8080/api/dev/token/complete -H "Content-Type: application/json" \
     -d '{"continuationToken":"…","code":"12345678"}'
# → {"accessToken":"eyJ…","tokenType":"Bearer","expiresIn":3599}
```

The continuation token is opaque: send it back unchanged. The Langa user is created on the first API call
with the new token, as for a sign-up through the frontend.

Errors: unknown user without `signUp` (400-101), account that cannot sign in with an email code (400-300),
expired sign-in session (400-301), sign-up requiring attributes other than the display name (400-302),
invalid continuation token (400-303), wrong code (401-001), identity provider error (502-000).
