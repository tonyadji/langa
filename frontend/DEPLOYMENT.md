# Langa Dashboard - Deployment Guide

**Document Type:** Deployment Guide  
**Audience:** DevOps Engineers, System Administrators  
**Purpose:** Complete deployment instructions for production environments

---

## Table of Contents

1. [Quick Start](#quick-start)
2. [Docker Deployment](#docker-deployment)
3. [VPS Deployment with Nginx](#vps-deployment-with-nginx)
4. [Environment Configuration](#environment-configuration)
5. [CI/CD Setup](#cicd-setup)
6. [Troubleshooting](#troubleshooting)
7. [Production Checklist](#production-checklist)

---

## Quick Start

### Prerequisites

- Docker 20.10+ with buildx support
- (Optional) VPS or cloud server (AMD64/Intel architecture)
- (Optional) Domain name and SSL certificate for HTTPS

### Local Production Build

```bash
# Build Docker image
docker buildx build \
  --platform linux/amd64 \
  --build-arg VITE_API_BASE_URL=https://langa-production.up.railway.app/api \
  -t langa-dashboard:latest \
  .

# Run container
docker run -d --name langa-dashboard -p 3000:80 langa-dashboard:latest

# Test deployment
curl http://localhost:3000
curl http://localhost:3000/health
```

---

## Docker Deployment

### Multi-Stage Build

The application uses a multi-stage Docker build for optimal production deployment:

**Stage 1 - Builder:**
- Base: `node:20-alpine`
- Installs dependencies
- Compiles TypeScript
- Builds production bundle with Vite
- Output: `dist/` directory

**Stage 2 - Server:**
- Base: `nginx:alpine`
- Copies built files from Stage 1
- Configures nginx for SPA routing
- Adds security headers
- Enables gzip compression
- Final image size: ~50MB

### Build Arguments

| Argument | Description | Required | Default |
|----------|-------------|----------|---------|
| `VITE_API_BASE_URL` | Backend API endpoint URL | Yes | `http://localhost:3000/api` |

### Building the Image

**For Railway backend:**

```bash
docker buildx build \
  --platform linux/amd64 \
  --build-arg VITE_API_BASE_URL=https://langa-production.up.railway.app/api \
  -t langa-dashboard:latest \
  .
```

**For custom backend:**

```bash
docker buildx build \
  --platform linux/amd64 \
  --build-arg VITE_API_BASE_URL=https://your-api.example.com/api \
  -t langa-dashboard:latest \
  .
```

**Important:** Always specify `--platform linux/amd64` for compatibility with most VPS servers.

### Running the Container

**Simple run:**

```bash
docker run -d \
  --name langa-dashboard \
  -p 3000:80 \
  --restart unless-stopped \
  langa-dashboard:latest
```

**With health checks:**

```bash
docker run -d \
  --name langa-dashboard \
  -p 3000:80 \
  --restart unless-stopped \
  --health-cmd="wget --quiet --tries=1 --spider http://localhost/health || exit 1" \
  --health-interval=30s \
  --health-timeout=10s \
  --health-retries=3 \
  langa-dashboard:latest
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
services:
  langa-dashboard:
    image: ktac95/langa-dashboard:latest
    platform: linux/amd64
    container_name: langa-dashboard
    restart: unless-stopped
    ports:
      - "3000:80"
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 10s
      retries: 3
      start_period: 40s
```

**Deploy:**

```bash
docker compose up -d
```

**Update:**

```bash
docker compose pull
docker compose up -d --force-recreate
```

---

## VPS Deployment with Nginx

### Complete Production Setup

#### Step 1: Prepare the VPS

```bash
# Update system
sudo apt update && sudo apt upgrade -y

# Install Docker
curl -fsSL https://get.docker.com -o get-docker.sh
sudo sh get-docker.sh
sudo usermod -aG docker $USER

# Install Docker Compose
sudo apt install docker-compose -y

# Install nginx
sudo apt install nginx -y

# Install certbot for SSL
sudo apt install certbot python3-certbot-nginx -y
```

#### Step 2: Configure Firewall

**For firewall-cmd (RHEL/CentOS):**

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --reload
```

**For ufw (Ubuntu/Debian):**

```bash
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw allow 3000/tcp
sudo ufw reload
```

#### Step 3: Obtain SSL Certificate

```bash
# Using Let's Encrypt
sudo certbot --nginx -d langa.yourdomain.com

# Test auto-renewal
sudo certbot renew --dry-run
```

#### Step 4: Configure Nginx

Create `/etc/nginx/sites-available/langa-dashboard`:

```nginx
# HTTP → HTTPS redirect
server {
    listen 80;
    server_name langa.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS server
server {
    listen 443 ssl http2;
    server_name langa.yourdomain.com;

    # SSL certificates (managed by certbot)
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers 'ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384';
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;

    # Proxy to Docker container
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Proxy timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
}
```

Enable the site:

```bash
sudo ln -s /etc/nginx/sites-available/langa-dashboard /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

#### Step 5: Deploy Application

```bash
# Create application directory
mkdir -p ~/langa-dashboard
cd ~/langa-dashboard

# Create docker-compose.yml (see Docker Compose section above)

# Pull and start
docker compose pull
docker compose up -d

# Verify
docker compose ps
docker compose logs -f
```

#### Step 6: Verify Deployment

```bash
# Test HTTP redirect
curl -I http://langa.yourdomain.com

# Test HTTPS
curl -I https://langa.yourdomain.com

# Test health endpoint
curl https://langa.yourdomain.com/health

# Check SSL certificate
openssl s_client -connect langa.yourdomain.com:443 -servername langa.yourdomain.com
```

---

## Environment Configuration

### Build-Time Environment Variables

Environment variables must be set during Docker build (not runtime) because Vite bundles them into the JavaScript:

```bash
docker buildx build \
  --build-arg VITE_API_BASE_URL=https://api.example.com/api \
  -t langa-dashboard .
```

### Available Variables

Create `.env.production`:

```env
# Backend API URL (REQUIRED)
VITE_API_BASE_URL=https://langa-production.up.railway.app/api

# Application name
VITE_APP_NAME=Langa Dashboard

# Enable debug mode (set to false in production)
VITE_ENABLE_DEBUG=false
```

### Multiple Environments

**Development:**
```bash
docker buildx build \
  --build-arg VITE_API_BASE_URL=http://localhost:8080/api \
  -t langa-dashboard:dev .
```

**Staging:**
```bash
docker buildx build \
  --build-arg VITE_API_BASE_URL=https://staging-api.example.com/api \
  -t langa-dashboard:staging .
```

**Production:**
```bash
docker buildx build \
  --build-arg VITE_API_BASE_URL=https://api.example.com/api \
  -t langa-dashboard:latest .
```

---

## CI/CD Setup

### GitHub Actions Workflow

Create `.github/workflows/deploy-vps.yml`:

```yaml
name: Deploy to VPS

on:
  push:
    branches: [main]
  workflow_dispatch:

jobs:
  build-and-deploy:
    runs-on: ubuntu-latest
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v3
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v2
      
      - name: Login to Docker Hub
        uses: docker/login-action@v2
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: ./frontend
          platforms: linux/amd64
          push: true
          tags: ${{ secrets.DOCKER_USERNAME }}/langa-dashboard:latest
          build-args: |
            VITE_API_BASE_URL=${{ secrets.API_BASE_URL }}
      
      - name: Deploy to VPS
        uses: appleboy/ssh-action@master
        with:
          host: ${{ secrets.VPS_HOST }}
          username: ${{ secrets.VPS_USERNAME }}
          key: ${{ secrets.VPS_SSH_KEY }}
          script: |
            cd ~/langa-dashboard
            docker compose pull
            docker compose up -d --force-recreate
```

### Required Secrets

Configure in GitHub repository settings:

- `DOCKER_USERNAME`: Docker Hub username
- `DOCKER_PASSWORD`: Docker Hub password or access token
- `API_BASE_URL`: Backend API URL
- `VPS_HOST`: VPS IP address or hostname
- `VPS_USERNAME`: SSH username
- `VPS_SSH_KEY`: SSH private key

---

## Troubleshooting

### CSS Not Loading

**Symptoms:** Application loads but has no styling

**Diagnosis:**

```bash
# Check if CSS file exists
docker exec langa-dashboard ls -la /usr/share/nginx/html/assets/

# Test CSS file directly
curl -I http://localhost:3000/assets/index-*.css

# Check Vite base configuration
docker exec langa-dashboard cat /usr/share/nginx/html/index.html | grep -E "(href|src)"
```

**Solution:** Rebuild with correct base path (should be `/` in vite.config.ts)

### API Connection Refused

**Symptoms:** Frontend loads but can't reach backend

**Diagnosis:**

```bash
# Check what API URL was baked into the build
docker exec langa-dashboard cat /usr/share/nginx/html/assets/index-*.js | grep -o 'https://[^"]*api' | head -1

# Test API from your browser
curl https://langa-production.up.railway.app/api/health
```

**Solution:** Rebuild with correct `VITE_API_BASE_URL`

### Container Exits Immediately

**Symptoms:** Container starts then stops

**Diagnosis:**

```bash
# Check logs
docker logs langa-dashboard

# Verify nginx configuration
docker exec langa-dashboard nginx -t

# Check if files were copied
docker exec langa-dashboard ls -la /usr/share/nginx/html/
```

**Solution:** Usually indicates missing files or nginx config error

### Platform Architecture Mismatch

**Symptoms:** "exec format error" when starting container

**Diagnosis:**

```bash
# Check container architecture
docker inspect langa-dashboard | grep Architecture
```

**Solution:** Rebuild with `--platform linux/amd64` flag

### SSL Certificate Errors

**Symptoms:** Browser shows "Not Secure" or certificate warnings

**Diagnosis:**

```bash
# Check certificate files
sudo ls -la /etc/letsencrypt/live/yourdomain.com/

# Verify nginx config
sudo nginx -t

# Check certificate expiry
sudo certbot certificates

# Test SSL
curl -vI https://langa.yourdomain.com
```

**Solution:** Renew certificate with `sudo certbot renew`

---

## Production Checklist

### Pre-Deployment

- [ ] Configure `VITE_API_BASE_URL` for production backend
- [ ] Build Docker image for `linux/amd64` platform
- [ ] Test Docker image locally
- [ ] Push image to Docker registry
- [ ] Verify backend API is accessible
- [ ] Configure DNS records for domain
- [ ] Obtain SSL certificates

### Infrastructure

- [ ] VPS provisioned with adequate resources (2GB+ RAM recommended)
- [ ] Docker and Docker Compose installed
- [ ] Nginx installed and configured
- [ ] Firewall configured (ports 80, 443, 3000)
- [ ] SSL certificates installed and valid
- [ ] Auto-renewal configured for SSL (certbot)

### Deployment

- [ ] Pull latest Docker image
- [ ] Start container with docker-compose
- [ ] Verify container health check passes
- [ ] Test HTTP → HTTPS redirect
- [ ] Verify assets load correctly (CSS, JS, images)
- [ ] Test API connectivity from frontend
- [ ] Test user authentication flow
- [ ] Verify application functionality

### Post-Deployment

- [ ] Set up monitoring and alerting
- [ ] Configure log aggregation
- [ ] Set up automated backups
- [ ] Document deployment process
- [ ] Create rollback procedure
- [ ] Test disaster recovery
- [ ] Monitor performance metrics
- [ ] Schedule regular security updates

### Security

- [ ] HTTPS enforced (HTTP redirects to HTTPS)
- [ ] Security headers configured
- [ ] SSL/TLS using modern protocols (TLS 1.2+)
- [ ] Strong cipher suites configured
- [ ] Regular security updates scheduled
- [ ] Access logs reviewed regularly
- [ ] Fail2ban or similar brute-force protection (optional)

---

## Performance Optimization

### Build Optimization

Current production bundle sizes:
- CSS: 43.10 kB (gzipped: 8.04 kB)
- React vendor: 31.78 kB (gzipped: 11.18 kB)
- UI vendor: 347.93 kB (gzipped: 101.65 kB)
- Total JS: ~620 kB (gzipped: ~180 kB)

### Nginx Optimization

The container nginx is already configured with:
- Gzip compression enabled
- 1-year cache for static assets
- Proper MIME types
- Security headers

### CDN Integration (Optional)

For global deployments, consider using a CDN:

```nginx
# Add to nginx proxy config
location /assets/ {
    proxy_pass http://localhost:3000/assets/;
    proxy_cache_valid 200 1y;
    add_header Cache-Control "public, immutable";
    add_header CDN-Cache-Control "public, max-age=31536000";
}
```

---

## Support and Resources

- **Documentation:** [docs/](./documents/)
- **Issues:** [GitHub Issues](https://github.com/your-org/langa/issues)
- **Docker Hub:** [ktac95/langa-dashboard](https://hub.docker.com/r/ktac95/langa-dashboard)
- **Production Backend:** https://langa-production.up.railway.app

---

**Last Updated:** January 17, 2026  
**Version:** 1.0.0
