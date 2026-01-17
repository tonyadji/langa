# Langa Dashboard - Quick Deployment Reference

## Build & Deploy Commands

### Build Docker Image
```bash
docker buildx build \
  --platform linux/amd64 \
  --build-arg VITE_API_BASE_URL=https://langa-production.up.railway.app/api \
  -t ktac95/langa-dashboard:latest \
  .
```

### Push to Registry
```bash
docker push ktac95/langa-dashboard:latest
```

### Deploy on VPS
```bash
ssh langa@217.160.3.153
cd ~/langa-dashboard
docker compose pull
docker compose down
docker compose up -d
```

## Quick Checks

### Container Status
```bash
docker ps | grep langa-dashboard
docker logs langa-dashboard --tail 50
```

### Health Check
```bash
curl http://localhost:3000/health
curl https://langa.capricedumardi.com/health
```

### Verify Assets
```bash
docker exec langa-dashboard ls -la /usr/share/nginx/html/assets/
curl -I https://langa.capricedumardi.com/assets/index-*.css
```

### Test API Connection
```bash
curl https://langa-production.up.railway.app/api/health
```

## Common Issues

### CSS Not Loading
```bash
# Rebuild with correct base path
docker buildx build --build-arg VITE_API_BASE_URL=... -t langa-dashboard .
```

### Container Won't Start
```bash
docker logs langa-dashboard
docker exec langa-dashboard nginx -t
```

### SSL Certificate Error
```bash
sudo certbot certificates
sudo certbot renew
sudo systemctl reload nginx
```

## Production URLs

- **Frontend:** https://langa.capricedumardi.com
- **Backend API:** https://langa-production.up.railway.app/api
- **Docker Hub:** ktac95/langa-dashboard:latest
- **VPS IP:** 217.160.3.153

## Key Files

- `Dockerfile` - Multi-stage build configuration
- `docker-compose.yml` - Container orchestration
- `vite.config.ts` - Vite build configuration
- `/etc/nginx/sites-available/langa-dashboard` - Nginx config on VPS

## Emergency Rollback

```bash
# Pull specific version
docker pull ktac95/langa-dashboard:v1.0.0

# Update docker-compose.yml to use specific tag
# Then restart
docker compose up -d --force-recreate
```

---

**For full documentation, see:**
- [DEPLOYMENT.md](./DEPLOYMENT.md)
- [CHANGELOG.md](./CHANGELOG.md)
- [DEPLOYMENT-SUMMARY.md](./DEPLOYMENT-SUMMARY.md)
