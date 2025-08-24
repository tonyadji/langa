# 🚀 Monitoring Platform (Portfolio Project)

![Java](https://img.shields.io/badge/Java-17-orange?logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3-green?logo=springboot&logoColor=white)
![React](https://img.shields.io/badge/React-18-blue?logo=react&logoColor=white)
![MongoDB](https://img.shields.io/badge/Database-MongoDB-brightgreen?logo=mongodb&logoColor=white)
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
- Authentication with JWT (frontend ↔ backend)  
- Pagination and filtering of logs  

---

## 🚀 Quick Start

### 1️⃣ Clone the repo
```bash
git clone https://github.com/tonyadji/langa.git
cd <langa>---

## 🌟 Features

- Collect application logs via the **agent**  
- Store and expose logs through the **backend** REST API  
- Visualize logs in a clean **React dashboard**  
- Authentication with JWT (frontend ↔ backend)  
- Pagination and filtering of logs  

---

## 🚀 Quick Start

### 1️⃣ Clone the repo
```bash
git clone https://github.com/tonyadji/langa.git
cd <langa>

2️⃣ Start each service

Backend
cd backend
./mvnw spring-boot:run

Frontend
cd frontend
npm install
npm start

register an account and create an application, so that you can configure the agent to send logs

📡 API Overview
Some useful endpoints from the backend:

GET /api/applications → list applications

GET /api/applications/{id}/logs?page=0&size=20 → get paginated logs for an application

POST /api/auth/login → authenticate and get a JWT