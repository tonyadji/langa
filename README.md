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

## 🔐 Authentication (Microsoft Entra External ID)

Users and tokens are managed by a Microsoft Entra External ID tenant: the frontend signs users in with MSAL
(authorization code + PKCE) and the backend only validates the access tokens. On the first request of a user,
the backend creates the Langa user, or links an existing one having the same email (account key and data are kept).

Tenant setup:

1. App registration **langa-api**: expose the scope `access_as_user`, set `accessTokenAcceptedVersion` to `2`
   in the manifest and add the optional claim `email` to the access token.
2. App registration **langa-spa**: platform *Single-page application* with the frontend URLs as redirect URIs,
   delegated permission `access_as_user` on langa-api (admin consent granted).
3. User flow *Sign up and sign in* (email + password or one-time code) linked to langa-spa.

Backend settings:

| Variable | Example |
|---|---|
| `ENTRA_ISSUER_URI` | `https://<tenant-id>.ciamlogin.com/<tenant-id>/v2.0` |
| `ENTRA_JWK_SET_URI` | `https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/discovery/v2.0/keys` |
| `ENTRA_API_AUDIENCES` | `<langa-api client id>,api://<langa-api client id>` |
| `ENTRA_API_REQUIRED_SCOPE` | `access_as_user` (default) |

Frontend settings (build time):

| Variable | Example |
|---|---|
| `VITE_ENTRA_CLIENT_ID` | `<langa-spa client id>` |
| `VITE_ENTRA_AUTHORITY` | `https://<tenant-subdomain>.ciamlogin.com/` |
| `VITE_ENTRA_API_SCOPE` | `api://<langa-api client id>/access_as_user` |
