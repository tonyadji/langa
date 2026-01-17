# Token Refresh Mechanism - Documentation

## Overview

Le système de refresh de tokens JWT dans Langa Dashboard utilise une approche à **deux niveaux** pour garantir que les utilisateurs restent authentifiés sans interruption :

1. **Refresh Proactif** (useTokenRefresh hook) - Préventif
2. **Refresh Réactif** (Axios interceptor) - En cas d'erreur

---

## Architecture

### 1. Refresh Proactif (`useTokenRefresh` hook)

**Fichier**: `src/features/auth/hooks/useTokenRefresh.ts`

**Fonctionnement**:
- Vérifie l'expiration du token **toutes les 60 secondes**
- Decode le JWT pour lire le champ `exp` (expiration timestamp)
- Si le token expire dans **moins de 5 minutes**, déclenche un refresh automatique
- Stocke les nouveaux tokens (access + refresh) dans localStorage

**Avantages**:
- ✅ Évite les interruptions utilisateur (refresh avant expiration)
- ✅ Réduit les erreurs 401/403
- ✅ Améliore l'expérience utilisateur (pas de déconnexion surprise)

**Code**:
```typescript
// Dans App.tsx
function AppContent() {
  useTokenRefresh(); // Active le refresh proactif
  return <AppRouter />;
}
```

---

### 2. Refresh Réactif (Axios Interceptor)

**Fichier**: `src/services/api.ts`

**Fonctionnement**:
- Intercepte toutes les réponses HTTP avec status **401 ou 403**
- Vérifie si l'endpoint est sécurisé (ignore `/auth/`, `/ingestion/`, etc.)
- Tente de refresh le token via `POST /auth/refresh`
- **Si succès**: Rejoue la requête originale avec le nouveau token
- **Si échec**: Logout automatique et redirect vers `/login`

**Gestion de la concurrence**:
- Queue les requêtes pendant un refresh en cours
- Empêche multiples appels simultanés à `/auth/refresh`
- Traite toutes les requêtes en attente après succès/échec

**Avantages**:
- ✅ Filet de sécurité si le refresh proactif échoue
- ✅ Gère les cas edge (token expire juste avant une requête)
- ✅ Transparent pour l'utilisateur (retry automatique)

**Code**:
```typescript
// L'interceptor détecte automatiquement 401/403
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (status === 401 || status === 403) {
      // Tentative de refresh...
    }
  }
);
```

---

## Flux de Refresh Token

### Scénario 1: Refresh Proactif (Cas Normal)

```
[T+0min]   User logged in, access token expires in 60 minutes
[T+55min]  useTokenRefresh détecte expiration < 5 min
           → POST /auth/refresh avec refreshToken
           → Reçoit nouveau accessToken + refreshToken
           → Stocke dans localStorage
[T+56min]  User continue normalement (pas d'interruption)
[T+115min] Cycle se répète automatiquement
```

### Scénario 2: Refresh Réactif (Fallback)

```
[User]     Fait une requête GET /api/applications
           → Access token expiré (pas détecté proactivement)
[Server]   Retourne 403 Forbidden
[Axios]    Interceptor détecte 403
           → POST /auth/refresh
           → Succès: Nouveau token
           → Rejoue GET /api/applications
[Server]   Retourne 200 OK avec données
[User]     Reçoit les données (ne voit pas l'erreur)
```

### Scénario 3: Logout Automatique (Token invalide)

```
[User]     Access token expiré
[Axios]    Interceptor tente refresh
[Server]   Refresh token aussi expiré → 401
[Axios]    Détecte échec refresh
           → localStorage.clear()
           → window.location.href = '/login'
[User]     Redirigé vers page login
```

---

## Configuration

### Endpoints Non-Sécurisés (Skip Auth)

Les endpoints suivants **ne nécessitent pas** de token et sont exclus de la logique de refresh:

```typescript
const UNSECURED_ENDPOINTS = [
  '/auth/',           // Login, register, refresh
  '/ingestion/',      // Log/metrics ingestion
  '/team-invitations/', // Public invitation links
  '/first-connection',
  '/swagger-ui',      // API docs
  '/actuator/',       // Health checks
];
```

### Durées de Vie

| Token | Durée (Backend) | Buffer Proactif | Vérification |
|-------|-----------------|-----------------|--------------|
| Access Token | 60 minutes | 5 minutes | Toutes les 60s |
| Refresh Token | 7 jours | N/A | Sur échec 401/403 |

---

## Gestion des Erreurs

### Cas 1: Réseau Indisponible

```typescript
// useTokenRefresh détecte une erreur réseau
catch (error) {
  console.error('✗ Proactive token refresh failed:', error);
  // Ne logout PAS immédiatement (peut être temporaire)
  // Laisse l'interceptor réactif gérer au prochain appel API
}
```

### Cas 2: Refresh Token Expiré

```typescript
// Interceptor détecte échec refresh
catch (refreshError) {
  // Logout définitif
  localStorage.removeItem('langa_auth_token');
  localStorage.removeItem('langa_refresh_token');
  window.location.href = '/login';
}
```

### Cas 3: Multiple Requêtes Simultanées

```typescript
// Queue system évite spam
if (isRefreshing) {
  return new Promise((resolve, reject) => {
    failedQueue.push({ resolve, reject });
  });
}
```

---

## Tests

### Tester le Refresh Proactif

1. Login avec un utilisateur
2. Ouvrir DevTools → Console
3. Attendre ~5 minutes avant expiration token
4. Observer le log: `✓ Token refreshed proactively`
5. Vérifier localStorage: nouveau `langa_auth_token`

### Tester le Refresh Réactif

1. Login avec un utilisateur
2. Ouvrir DevTools → Application → localStorage
3. Modifier `langa_auth_token` (invalider manuellement)
4. Faire une action (ex: naviguer vers /applications)
5. Observer Network tab: 403 → refresh → retry → 200

### Tester le Logout Automatique

1. Login avec un utilisateur
2. Ouvrir DevTools → Application → localStorage
3. Supprimer `langa_refresh_token`
4. Attendre expiration access token ou invalider manuellement
5. Faire une action → Redirect vers `/login`

---

## Debugging

### Logs de Debug

Ajouter dans `src/services/api.ts` pour verbose logging:

```typescript
console.log('[Token Refresh] Attempting refresh...');
console.log('[Token Refresh] Success - new token stored');
console.log('[Token Refresh] Failed:', error);
console.log('[Token Refresh] Queued request count:', failedQueue.length);
```

### Décoder un JWT Manuellement

```javascript
// Dans la console du navigateur
const token = localStorage.getItem('langa_auth_token');
const payload = JSON.parse(atob(token.split('.')[1]));
console.log('Expiration:', new Date(payload.exp * 1000));
console.log('Issued at:', new Date(payload.iat * 1000));
console.log('Subject (user):', payload.sub);
```

---

## Problèmes Connus et Solutions

### Problème: "Request failed with status code 403"

**Cause**: Token expiré et refresh échoue

**Solution**:
1. ✅ Vérifier que `/auth/refresh` endpoint existe backend
2. ✅ Vérifier format refresh token (JWT valide)
3. ✅ Vérifier durée de vie refresh token backend (≥ 7 jours recommandé)
4. ✅ S'assurer que l'interceptor gère 403 ET 401

### Problème: Boucle infinie de refresh

**Cause**: Interceptor tente refresh sur `/auth/refresh` lui-même

**Solution**:
```typescript
// Skip refresh sur endpoint refresh
if (url.includes('/auth/refresh')) {
  localStorage.clear();
  window.location.href = '/login';
  return Promise.reject(error);
}
```

### Problème: Multiple fenêtres/tabs désynchronisées

**Cause**: localStorage updates pas synchronisés entre tabs

**Solution Future**:
```typescript
// Écouter storage events
window.addEventListener('storage', (e) => {
  if (e.key === 'langa_auth_token' && !e.newValue) {
    // Token supprimé dans autre tab → logout cette tab aussi
    window.location.href = '/login';
  }
});
```

---

## Sécurité

### ✅ Bonnes Pratiques Implémentées

- **HTTPS Only**: Tokens jamais envoyés sur HTTP en production
- **HttpOnly Cookies** (Backend): Refresh token stocké en cookie sécurisé
- **Short-lived Access Tokens**: 60 minutes max
- **Rotation de Refresh Tokens**: Nouveau refresh token à chaque refresh
- **Pas de token dans URL**: Jamais exposé dans query params

### ⚠️ Limitations Actuelles

- **localStorage**: Vulnérable à XSS (considérer sessionStorage ou cookies)
- **Pas de validation côté client**: Le decode JWT est informatif, pas sécurisé
- **Pas de revocation côté serveur**: Blacklist tokens non implémenté

---

## Roadmap

### Court Terme
- [ ] Ajouter tests unitaires pour `useTokenRefresh`
- [ ] Ajouter tests d'intégration pour l'interceptor
- [ ] Améliorer logs de debug (niveaux: info, warn, error)

### Moyen Terme
- [ ] Synchronisation multi-tabs via BroadcastChannel API
- [ ] Retry avec exponential backoff sur échecs réseau
- [ ] Metrics: tracer taux de succès/échec refresh

### Long Terme
- [ ] Migration vers HttpOnly cookies (plus sécurisé)
- [ ] Implémentation PKCE pour refresh tokens
- [ ] Token rotation tracking côté backend

---

## Références

- [RFC 6749 - OAuth 2.0](https://datatracker.ietf.org/doc/html/rfc6749#section-6)
- [JWT Best Practices](https://datatracker.ietf.org/doc/html/rfc8725)
- [Axios Interceptors](https://axios-http.com/docs/interceptors)

