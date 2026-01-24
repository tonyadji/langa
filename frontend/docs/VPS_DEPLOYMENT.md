# Deployment Guide: Langa Dashboard to VPS

This guide explains how to deploy the Langa Dashboard to your VPS server using GitHub Actions.

**Important**: Le projet frontend se trouve dans le dossier `frontend/` du repository, pas à la racine. Le workflow GitHub Actions et les configurations sont adaptés en conséquence.

## Prerequisites

### 1. VPS Server Requirements

- Ubuntu 20.04+ or Debian 11+ (or any Linux distribution with Docker support)
- Minimum 2GB RAM, 2 CPU cores
- 10GB available disk space
- Public IP address or domain name
- SSH access with sudo privileges

### 2. Software Requirements on VPS

- Docker Engine 20.10+
- Docker Compose (optional, for easier management)
- SSH server running on port 22 (or custom port)

## VPS Setup

### Step 1: Verify Docker Installation (Rootless Configuration)

```bash
# Connect to your VPS
ssh user@your-vps-ip

# Verify Docker is running (rootless mode - no sudo needed)
docker --version
docker ps

# Check Docker context (should show rootless)
docker context ls
```

**Note**: Puisque vous avez déjà un Docker rootless fonctionnel avec Keycloak et PostgreSQL, aucune installation supplémentaire n'est nécessaire.

### Step 2: Create SSH Key for GitHub Actions

```bash
# Generate a new SSH key pair (on your VPS)
ssh-keygen -t ed25519 -C "github-actions-deploy" -f ~/.ssh/github_deploy

# Add the public key to authorized_keys
cat ~/.ssh/github_deploy.pub >> ~/.ssh/authorized_keys
chmod 600 ~/.ssh/authorized_keys

# Display the private key (copy this to GitHub Secrets)
cat ~/.ssh/github_deploy
```

**Important**: Copy the entire private key output (including `-----BEGIN OPENSSH PRIVATE KEY-----` and `-----END OPENSSH PRIVATE KEY-----`)

### Step 3: Configure Firewall (If Not Already Done)

```bash
# Check current firewall status
sudo firewall-cmd --state
sudo firewall-cmd --list-all

# If firewall is already configured for Keycloak/PostgreSQL, 
# just add the dashboard port
sudo firewall-cmd --permanent --add-port=3000/tcp

# Reload firewall to apply changes
sudo firewall-cmd --reload

# Verify the port was added
sudo firewall-cmd --list-ports
```

**Note**: Since you already have services running, your firewall is likely already configured. Only add the specific port for the dashboard.

## GitHub Repository Setup

### Step 1: Add GitHub Secrets

Navigate to your GitHub repository:
**Settings → Secrets and variables → Actions → New repository secret**

Add the following secrets:

#### Docker Registry Credentials

| Secret Name | Description | Example |
|------------|-------------|---------|
| `DOCKER_USERNAME` | Docker Hub username | `yourusername` |
| `DOCKER_PASSWORD` | Docker Hub access token or password | Generate at hub.docker.com/settings/security |

**Alternative**: For GitHub Container Registry (ghcr.io):
- Use your GitHub username for `DOCKER_USERNAME`
- Create a Personal Access Token (PAT) with `write:packages` scope for `DOCKER_PASSWORD`

#### VPS Access Credentials

| Secret Name | Description | Example |
|------------|-------------|---------|
| `VPS_HOST` | VPS IP address or domain | `203.0.113.50` or `app.example.com` |
| `VPS_USER` | SSH username on VPS | `root` or `deploy` |
| `VPS_SSH_KEY` | Private SSH key (from Step 2) | Entire content of `~/.ssh/github_deploy` |
| `VPS_PORT` | SSH port (optional, default: 22) | `22` |

#### Application Configuration

| Secret Name | Description | Example |
|------------|-------------|---------|
| `VITE_API_BASE_URL` | Backend API URL | `https://api.langa.example.com` |
| `APP_PORT` | Port to expose on VPS (default: 3000) | `3000` |

**Note**: L'application sera accessible sur le port 3000 du VPS (`http://your-vps-ip:3000`). Nginx proxie ensuite ce port vers HTTPS.


#### Optional Secrets

| Secret Name | Description |
|------------|-------------|
| `SLACK_WEBHOOK_URL` | Slack webhook for deployment notifications |
| `SENTRY_DSN` | Sentry DSN for error tracking |

### Step 2: Create Docker Hub Access Token

1. Go to [Docker Hub](https://hub.docker.com)
2. Sign in to your account
3. Navigate to **Account Settings → Security**
4. Click **New Access Token**
5. Give it a name (e.g., "GitHub Actions")
6. Copy the token and save it as `DOCKER_PASSWORD` secret

## Deployment

### Deployment Methods

The workflow supports two deployment methods:

#### Method 1: Docker Run (Default)
Simple container deployment without docker-compose. Good for standalone deployment.

#### Method 2: Docker Compose (Recommended for Multi-Service Setup)
Uses docker-compose for better integration with existing services (Keycloak, PostgreSQL).

To use docker-compose:
1. Copy `docker-compose.yml` to your VPS: `~/langa-dashboard/docker-compose.yml`
2. Copy `docker-compose.env.example` to `~/langa-dashboard/.env` and configure
3. Update workflow environment variable: `DEPLOY_METHOD: docker-compose`

### Automatic Deployment

The workflow automatically triggers on:
- **Push to `main` branch**: Deploys to production
- **Manual trigger**: Use GitHub UI to select environment

### Manual Deployment

1. Go to your GitHub repository
2. Navigate to **Actions** tab
3. Select **Deploy to VPS** workflow
4. Click **Run workflow**
5. Select environment (production/staging)
6. Click **Run workflow**

### Monitor Deployment

1. Click on the running workflow to see real-time logs
2. Each job shows detailed progress:
   - **Build and Test**: Compiles app, runs tests
   - **Build Docker Image**: Creates and pushes Docker image
   - **Deploy to VPS**: Deploys to server
   - **Notify**: Sends status notification

## Verify Deployment

### Check Application Status

```bash
# SSH into your VPS
ssh user@your-vps-ip

# Method 1: If using docker run
docker ps | grep langa-dashboard
docker logs langa-dashboard --tail 50 -f

# Method 2: If using docker-compose
cd ~/langa-dashboard
docker-compose ps
docker-compose logs -f

# Test frontend accessibility
curl -I http://localhost:3000

# Test API connectivity from frontend perspective
curl https://api.langa.example.com/health
```

### Access Application

Open your browser and navigate to:
```
http://your-vps-ip:3000
```

Ou avec le domaine configuré:
```
https://langa.capricedumardi.com
```

## Integration with Backend API

### Frontend Architecture

Le dashboard frontend (React) est une application statique qui :
- **Communique uniquement avec l'API backend** via `VITE_API_BASE_URL`
- **Ne se connecte PAS directement** à Keycloak ou PostgreSQL
- Est servi par Nginx (dans le conteneur Docker)

### Backend Integration

**Important**: Seul votre backend API doit être connecté à Keycloak et PostgreSQL.

```
┌─────────────────┐
│  Frontend       │
│  (React/Nginx)  │  ← Accessible publiquement
└────────┬────────┘
         │ HTTP/HTTPS
         ▼
┌─────────────────┐
│  Backend API    │  ← Sur le même VPS
└────────┬────────┘
         │
    ┌────┴────┐
    ▼         ▼
┌─────────┐ ┌──────────┐
│Keycloak │ │PostgreSQL│
└─────────┘ └──────────┘
```

### Configuration du Backend

C'est le **backend** qui doit être sur le réseau `langa-network` avec Keycloak/PostgreSQL:

```bash
# Sur votre VPS, connecter le backend au réseau (pas le frontend)
docker network connect langa-network <backend-container-name>
```

### Variables d'environnement

**Frontend** (ce projet):
```bash
VITE_API_BASE_URL=https://api.langa.example.com  # URL publique de l'API
```

**Backend** (votre API Langa):
```bash
DATABASE_HOST=postgres          # Nom du conteneur PostgreSQL
DATABASE_PORT=5432
KEYCLOAK_URL=http://keycloak:8080  # URL interne
```

## SSL/HTTPS Setup (Recommended)

### Option 1: Nginx Reverse Proxy (Recommandé)

Puisque vous utilisez déjà Nginx pour vos autres services (Keycloak, PostgreSQL), ajoutez simplement une nouvelle configuration pour le dashboard:

```bash
# Créer la configuration Nginx pour le dashboard
sudo nano /etc/nginx/sites-available/langa-dashboard
```

Ajoutez cette configuration:

```nginx
server {
    listen 80;
    server_name langa.capricedumardi.com;

    # Redirection HTTPS (si certificat déjà configuré)
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name langa.capricedumardi.com;

    # Certificats SSL (déjà configurés pour capricedumardi.com)
    ssl_certificate /etc/letsencrypt/live/capricedumardi.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/capricedumardi.com/privkey.pem;
    
    # Ou si vous avez un certificat wildcard:
    # ssl_certificate /etc/letsencrypt/live/capricedumardi.com/fullchain.pem;
    # ssl_certificate_key /etc/letsencrypt/live/capricedumardi.com/privkey.pem;

    # Configuration SSL recommandée
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

```bash
# Activer le site
sudo ln -s /etc/nginx/sites-available/langa-dashboard /etc/nginx/sites-enabled/

# Tester la configuration
sudo nginx -t

# Recharger Nginx
sudo systemctl reload nginx
```

**Note**: Puisque votre certificat SSL est déjà configuré pour `capricedumardi.com`, il couvre automatiquement `langa.capricedumardi.com` (si c'est un certificat wildcard `*.capricedumardi.com`). Sinon, ajoutez simplement le sous-domaine au certificat existant:

```bash
# Si besoin d'ajouter le sous-domaine au certificat existant
sudo certbot certonly --nginx -d capricedumardi.com -d langa.capricedumardi.com

# Ou si certificat wildcard:
sudo certbot certonly --nginx -d capricedumardi.com -d *.capricedumardi.com
```

**Résultat**: Votre dashboard sera accessible via `https://langa.capricedumardi.com` avec le certificat SSL existant.

### Option 2: Traefik Reverse Proxy (Alternative)

**Utilisez cette option uniquement si vous préférez Traefik à Nginx.**

```yaml
# Dans docker-compose.yml, ajoutez:
services:
  langa-dashboard:
    labels:
      - "traefik.enable=true"
      - "traefik.http.routers.dashboard.rule=Host(`langa.capricedumardi.com`)"
      - "traefik.http.routers.dashboard.entrypoints=websecure"
      - "traefik.http.routers.dashboard.tls.certresolver=letsencrypt"
      - "traefik.http.services.dashboard.loadbalancer.server.port=80"
    networks:
      - traefik-public
```

## Rootless Docker Considerations

### User Permissions

```bash
# All Docker commands run as your user (no sudo needed)
docker ps
docker logs langa-dashboard

# Check Docker rootless setup
systemctl --user status docker

# Docker socket location (rootless)
echo $DOCKER_HOST
# Should show: unix:///run/user/1000/docker.sock (or similar)
```

### Port Binding Limitations

Rootless Docker cannot bind to ports < 1024 by default:

```bash
# Option 1: Use ports >= 1024 (recommended)
# Dashboard: 3000
# Keycloak: 8080
# Then use reverse proxy (Traefik/Nginx) for ports 80/443

# Option 2: Allow rootless to bind privileged ports (not recommended)
sudo setcap cap_net_bind_service=ep $(which rootlesskit)
```

### Resource Limits

Rootless Docker has different cgroup handling:

```yaml
# In docker-compose.yml - use deploy section (not resources)
deploy:
  resources:
    limits:
      cpus: '0.5'
      memory: 512M
```

## Rollback Procedure

If deployment fails or issues occur:

```bash
# SSH to VPS
ssh user@your-vps-ip

# Method 1: If using docker run
# List Docker images
docker images | grep langa-dashboard

# Stop current container
docker stop langa-dashboard
docker rm langa-dashboard

# Run previous version (replace TAG with previous tag)
docker run -d \
  --name langa-dashboard \
  --restart unless-stopped \
  -p 3000:80 \
  -e VITE_API_BASE_URL=https://api.langa.example.com \
  docker.io/yourusername/langa-dashboard:TAG

# Method 2: If using docker-compose
cd ~/langa-dashboard

# Edit .env to use previous image tag
nano .env  # Change: langa-dashboard:TAG

# Restart with previous version
docker-compose down
docker-compose up -d
```

## Environment-Specific Deployment

### Staging Environment

1. Create staging secrets in GitHub
2. Update workflow to use staging secrets for staging branch
3. Deploy to staging VPS or use different port on same VPS

Example staging configuration:
```yaml
# In .github/workflows/deploy-vps.yml
on:
  push:
    branches:
      - staging
```

## Troubleshooting

### SSH Connection Issues

```bash
# Test SSH connection from local machine
ssh -i ~/.ssh/your-key user@vps-ip

# Check SSH service on VPS
systemctl status sshd

# View SSH logs
journalctl -u sshd -f
```

**Note**: On rootless Docker VPS, avoid using `sudo` unless necessary.

### Docker Issues

```bash
# Check Docker service (rootless)
systemctl --user status docker

# View Docker logs (rootless)
journalctl --user -u docker -f

# Restart Docker (rootless)
systemctl --user restart docker

# Check Docker context
docker context ls
```

### Container Not Starting

```bash
# View container logs
docker logs langa-dashboard

# Inspect container
docker inspect langa-dashboard

# Check available disk space
df -h

# Check memory usage
free -h
```

### Port Already in Use

```bash
# Find process using port 3000
sudo lsof -i :3000

# Kill process (if needed)
sudo kill -9 <PID>
```

## Monitoring and Maintenance

### Log Rotation

```bash
# Configure Docker log rotation (already in docker-compose.yml)
# Verify configuration:
docker inspect langa-dashboard --format='{{.HostConfig.LogConfig}}'

# For rootless Docker, logs are in:
~/.local/share/docker/containers/<container-id>/<container-id>-json.log
```

### Resource Monitoring

```bash
# Install monitoring tools (if not already present)
# apt install htop  # No sudo needed if using user-installed tools

# View resource usage
htop

# View Docker stats for all containers
docker stats

# View specific container stats
docker stats langa-dashboard

# Check disk usage
df -h
du -sh ~/  # Check home directory size (rootless Docker stores data here)
```

### Backup Strategy

```bash
# Backup Docker volumes (if using)
docker run --rm -v langa_data:/data -v $(pwd):/backup ubuntu tar czf /backup/langa-backup.tar.gz /data

# Backup environment files
cp -r /path/to/env /backup/
```

## Security Best Practices

1. **Use SSH keys** instead of passwords
2. **Disable root SSH login**: Set `PermitRootLogin no` in `/etc/ssh/sshd_config`
3. **Use firewall** (UFW or iptables)
4. **Keep system updated**: `sudo apt update && sudo apt upgrade`
5. **Use secrets management** for sensitive data
6. **Enable Docker Content Trust**: `export DOCKER_CONTENT_TRUST=1`
7. **Regular security audits**: `docker scan image-name`

## Performance Optimization

### Enable Gzip Compression

Already configured in Dockerfile's Nginx setup.

### CDN Integration

Consider using Cloudflare or similar CDN for:
- DDoS protection
- Global content delivery
- SSL/TLS termination
- Caching static assets

### Monitoring Tools

- **Application Performance**: Sentry, New Relic
- **Server Monitoring**: Prometheus + Grafana, Datadog
- **Uptime Monitoring**: UptimeRobot, Pingdom

## Cost Optimization

### VPS Provider Recommendations

| Provider | Starting Price | Region Availability |
|----------|---------------|---------------------|
| DigitalOcean | $6/month | Global |
| Linode | $5/month | Global |
| Vultr | $5/month | Global |
| Hetzner | €4.5/month | Europe |
| AWS Lightsail | $5/month | Global |

### Resource Right-Sizing

Monitor usage for 1-2 weeks, then adjust VPS size based on actual needs.

## Additional Resources

- [Docker Best Practices](https://docs.docker.com/develop/dev-best-practices/)
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Nginx Configuration Guide](https://nginx.org/en/docs/)
- [Let's Encrypt Documentation](https://letsencrypt.org/docs/)

## Support

For issues or questions:
1. Check GitHub Issues
2. Review workflow logs
3. Check VPS system logs
4. Contact VPS provider support

---

**Last Updated**: January 16, 2026
