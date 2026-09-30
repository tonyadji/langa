# Authentication

Langa does not store passwords. Users and tokens are managed by an external **OpenID Connect** provider:
Microsoft Entra External ID today, any OIDC provider (Amazon Cognito, Keycloak, Auth0…) tomorrow.

- The **dashboard** signs users in (authorization code + PKCE) and sends the access token to the API.
- The **backend** is a pure OAuth2 resource server: it only validates access tokens.
- On the first request of a user, the backend creates the Langa user, or links an existing one with the same
  email (account key and data are kept).

> Log ingestion from the agent does **not** use OIDC: it is authenticated with a per-application HMAC
> signature (see [backend/documents](../backend/documents/01-SPECIFICATION.md#43-ingestion-security)).

## Contents

- [How the provider is isolated](#how-the-provider-is-isolated)
- [Backend settings](#backend-settings)
- [Microsoft Entra External ID setup](#microsoft-entra-external-id-setup)
- [Amazon Cognito (backend ready, frontend not yet)](#amazon-cognito-backend-ready-frontend-not-yet)
- [Getting a token without the dashboard (dev only)](#getting-a-token-without-the-dashboard-dev-only)

## How the provider is isolated

| Side | Abstraction | Where |
|---|---|---|
| Backend | Generic `application.security.auth` settings (`AUTH_*` variables) and the `ExternalIdentityResolver` port, which reads the identity from the token claims or from the OIDC UserInfo endpoint | `backend/.../infra/security/identity` |
| Frontend | `AuthClient` interface, selected with `VITE_AUTH_PROVIDER`. Pages and components only use `useAuth()` | `frontend/src/features/auth/providers` |

## Backend settings

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

## Microsoft Entra External ID setup

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

## Amazon Cognito (backend ready, frontend not yet)

1. User pool with email as sign-in attribute and self sign-up enabled, a domain (Hosted UI / managed login).
2. Resource server `langa-api` with a custom scope `access`.
3. Public app client (no secret), authorization code grant, scopes `openid email langa-api/access`,
   callback and sign-out URLs of the frontend.
4. Backend: the Cognito column above.
5. Frontend: implement `AuthClient` (e.g. `oidcAuthClient.ts` with `oidc-client-ts`, authority
   `https://cognito-idp.<region>.amazonaws.com/<user-pool-id>`, scope `openid email langa-api/access`;
   `register` redirects to `https://<cognito-domain>/signup`, `logout` to `https://<cognito-domain>/logout`),
   register it in `src/features/auth/providers/index.ts` under `cognito` and set `VITE_AUTH_PROVIDER=cognito`.

## Getting a token without the dashboard (dev only)

For API tests (Postman, curl…), the backend can return a real access token for a user, using the
**native authentication API** of Entra External ID with an email one-time code.

> ⚠️ Disabled by default. Never enable it in production.

**Entra setup** — an app registration **langa-test-client** with *Allow public client flows* and
*Enable native authentication* set to **Yes**, the delegated permission `access_as_user` on langa-api
(admin consent granted), linked to the user flow.

**Backend settings** (`application-local.yml`):

| Property | Example |
|---|---|
| `application.security.dev-token.enabled` | `true` (default `false`: the endpoint does not exist) |
| `application.security.dev-token.client-id` | `<langa-test-client client id>` |
| `application.security.dev-token.native-auth-uri` | `https://<tenant-subdomain>.ciamlogin.com/<tenant-id>` |
| `application.security.dev-token.scope` | `api://<langa-api client id>/access_as_user` |

**Usage**

```bash
# 1. A one-time code is sent to the user's email
curl -X POST http://localhost:8080/api/dev/token/start -H "Content-Type: application/json" \
     -d '{"username":"user@example.com"}'
# → {"continuationToken":"…","codeSentTo":"u***@example.com","codeLength":8,"signUp":false}

# To create the user in the identity provider if it does not exist (an existing user just signs in);
# displayName is optional (default: the part of the email before '@')
curl -X POST http://localhost:8080/api/dev/token/start -H "Content-Type: application/json" \
     -d '{"username":"new@example.com","signUp":true,"displayName":"New User"}'

# 2. Exchange the code for an access token (within a few minutes)
curl -X POST http://localhost:8080/api/dev/token/complete -H "Content-Type: application/json" \
     -d '{"continuationToken":"…","code":"12345678"}'
# → {"accessToken":"eyJ…","tokenType":"Bearer","expiresIn":3599}
```

The continuation token is opaque: send it back unchanged. The Langa user is created on the first API call
with the new token, as for a sign-up through the dashboard.

| Error code | Meaning |
|---|---|
| `400-101` | Unknown user without `signUp` |
| `400-300` | Account cannot sign in with an email code |
| `400-301` | Sign-in session expired |
| `400-302` | Sign-up requires attributes other than the display name |
| `400-303` | Invalid continuation token |
| `401-001` | Wrong code |
| `502-000` | Identity provider error |
