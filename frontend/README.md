# Langa Dashboard

Web dashboard of [Langa](../README.md): manage applications, explore logs and metrics, follow storage usage,
and collaborate with teams.

[![Frontend CI](https://github.com/tonyadji/langa/actions/workflows/frontend-ci.yml/badge.svg)](https://github.com/tonyadji/langa/actions/workflows/frontend-ci.yml)
![React](https://img.shields.io/badge/React-19-61DAFB?logo=react&logoColor=black)
![TypeScript](https://img.shields.io/badge/TypeScript-strict-3178C6?logo=typescript&logoColor=white)
![Vite](https://img.shields.io/badge/Vite-646CFF?logo=vite&logoColor=white)
![Tailwind CSS](https://img.shields.io/badge/Tailwind%20CSS-4-06B6D4?logo=tailwindcss&logoColor=white)

> [!NOTE]
> Work in progress — see the [roadmap](../README.md#roadmap-to-mvp).

## Contents

- [Features](#features)
- [Getting started](#getting-started)
- [Configuration](#configuration)
- [Scripts](#scripts)
- [Project structure](#project-structure)
- [Deployment](#deployment)
- [Troubleshooting](#troubleshooting)

## Features

| Area | What you can do |
|---|---|
| **Sign-in** | Sign up / sign in through Microsoft Entra External ID (OIDC, authorization code + PKCE via MSAL) |
| **Applications** | Create and delete applications, reveal and copy ingestion credentials, set the retention policy |
| **Logs** | Browse paginated logs, filter by level, keyword and time range |
| **Metrics** | Charts and tables of `@Monitored` method timings, with filters and statistics |
| **Usage** | Storage used by logs and metrics, trends over 7 / 30 / 90 days |
| **Sharing & teams** | Share an application with a user or a team, revoke access, create teams, invite members by e-mail, accept invitations |
| **UX** | Dark mode (follows the system), loading skeletons, error boundaries, keyboard navigation and skip link |

## Getting started

**Prerequisites:** Node.js 18+, a running [backend](../backend/README.md), and a SPA app registration in
Entra External ID ([setup](../docs/authentication.md#microsoft-entra-external-id-setup)).

```bash
cd frontend
npm install
cp .env.example .env.local   # then set the values below
npm run dev                  # http://localhost:5173
```

## Configuration

Vite variables are read at **build time** and end up in the browser: never put secrets in them.

| Variable | Required | Description |
|---|---|---|
| `VITE_API_BASE_URL` | yes | Backend API URL, e.g. `http://localhost:8080/api` |
| `VITE_AUTH_PROVIDER` | no | Identity provider implementation, default `entra` |
| `VITE_ENTRA_CLIENT_ID` | yes | Client ID of the `langa-spa` app registration |
| `VITE_ENTRA_AUTHORITY` | yes | `https://<tenant-subdomain>.ciamlogin.com/<tenant-id>/` |
| `VITE_ENTRA_API_SCOPE` | yes | `api://<langa-api client id>/access_as_user` |
| `VITE_ENTRA_REDIRECT_URI` · `VITE_ENTRA_POST_LOGOUT_REDIRECT_URI` | no | Default to the current origin and `<origin>/login` |

Put local values in `.env.local` (git-ignored). `.env.development` is versioned and takes precedence, so
do not define the `VITE_ENTRA_*` values there.

## Scripts

| Command | Purpose |
|---|---|
| `npm run dev` | Development server |
| `npm run build` · `npm run preview` | Production build and local preview |
| `npm test` · `npm run test:coverage` | Unit and integration tests (Vitest, Testing Library, MSW) |
| `npm run test:e2e` | End-to-end tests (Playwright) |
| `npm run lint` · `npm run check:types` | ESLint and TypeScript checks |
| `npm run check:all` | Types + lint + tests, as in CI |
| `npm run storybook` | Component catalogue on port 6006 |

## Project structure

```text
src/
├── features/           Feature modules: api client, hooks, components
│   ├── applications/
│   ├── auth/           AuthClient interface + MSAL implementation, useAuth()
│   ├── logs/
│   ├── metrics/
│   ├── teams/
│   └── usage/
├── pages/              Route-level pages
├── router/             Routes and ProtectedRoute
├── components/         Shared UI (layout, common widgets, error boundary…)
├── services/ · hooks/ · contexts/ · utils/ · types/
└── stories/            Storybook stories
tests/                  Integration tests (MSW mocks) and Playwright e2e tests
```

The identity provider is hidden behind the `AuthClient` interface (`src/features/auth/providers`): pages only
use `useAuth()`, so another OIDC provider can be added without touching them.

## Deployment

The production image is a multi-stage build served by nginx ([`Dockerfile`](Dockerfile)). On every push to
`main`, [frontend-deploy.yml](../.github/workflows/frontend-deploy.yml) deploys it to Railway;
[frontend-ci.yml](../.github/workflows/frontend-ci.yml) runs lint, type check, tests, build and a dependency
audit on every change.

```bash
docker build --build-arg VITE_API_BASE_URL=https://api.example.com/api -t langa-dashboard .
docker run -p 3000:80 langa-dashboard      # health check: GET /health
```

More options (Docker Compose, VPS with nginx and TLS): [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) and
[docs/VPS_DEPLOYMENT.md](docs/VPS_DEPLOYMENT.md).

## Troubleshooting

| Symptom | Fix |
|---|---|
| Page without styles | Tailwind CSS 4 needs the `@tailwindcss/vite` plugin in `vite.config.ts` |
| Sign-in fails with an issuer error | `VITE_ENTRA_AUTHORITY` must contain the tenant **id**, not only the subdomain |
| API calls blocked (CORS) | Add the dashboard URL to `CORS_ALLOWED_ORIGINS` on the backend |
| Port 5173 busy | `npm run dev -- --port 3000` |

## Credits

Dashboard co-authored by **Alex Kouasseu** and **Tony Adji**. Development history (specs, plans, progress
reports) is kept in [docs/archive](docs/archive/README.md).
