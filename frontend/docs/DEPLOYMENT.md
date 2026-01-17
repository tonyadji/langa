# Deployment Guide

This guide covers deploying the Langa Dashboard to various platforms.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Environment Configuration](#environment-configuration)
- [Vercel Deployment](#vercel-deployment)
- [Docker Deployment](#docker-deployment)
- [Self-Hosted Deployment](#self-hosted-deployment)
- [Post-Deployment](#post-deployment)

---

## Prerequisites

- Node.js >= 18.0.0
- npm >= 9.0.0
- Access to Langa backend API
- Domain name (optional but recommended)

---

## Environment Configuration

### Production Environment Variables

Create a `.env.production` file:

```env
# Production API endpoint
VITE_API_BASE_URL=https://api.langa.io

# Application info
VITE_APP_NAME=Langa Dashboard
VITE_APP_VERSION=1.0.0

# Feature flags
VITE_ENABLE_DARK_MODE=false
VITE_ENABLE_ANALYTICS=true

# Performance
VITE_API_TIMEOUT=30000
VITE_SESSION_TIMEOUT=60
```

### Build for Production

```bash
# Install dependencies
npm ci --only=production

# Build optimized bundle
npm run build

# Preview production build locally
npm run preview
```

The production build will be in the `dist/` directory.

---

## Vercel Deployment

### Option 1: Vercel CLI

1. Install Vercel CLI:
```bash
npm i -g vercel
```

2. Login to Vercel:
```bash
vercel login
```

3. Deploy:
```bash
# Development deployment
vercel

# Production deployment
vercel --prod
```

### Option 2: GitHub Integration

1. Push code to GitHub
2. Go to [vercel.com](https://vercel.com)
3. Click "Import Project"
4. Select your GitHub repository
5. Configure:
   - **Framework Preset**: Vite
   - **Build Command**: `npm run build`
   - **Output Directory**: `dist`
   - **Install Command**: `npm ci`

6. Add environment variables:
   - `VITE_API_BASE_URL`
   - `VITE_APP_NAME`
   - etc.

7. Click "Deploy"

### Vercel Configuration

The project includes `vercel.json` with:
- SPA routing rewrites
- Security headers
- Asset caching
- Redirects

### Custom Domain

1. Go to your Vercel project settings
2. Navigate to "Domains"
3. Add your custom domain
4. Update DNS records as instructed
5. Enable automatic HTTPS

---

## Docker Deployment

### Build Docker Image

```bash
# Build image
docker build -t langa-dashboard:latest .

# Tag for registry (optional)
docker tag langa-dashboard:latest registry.example.com/langa-dashboard:latest
```

### Run Container

```bash
# Run on port 8080
docker run -d \
  --name langa-dashboard \
  -p 8080:80 \
  --restart unless-stopped \
  langa-dashboard:latest

# Access at http://localhost:8080
```

### Docker Compose

Create `docker-compose.yml`:

```yaml
version: '3.8'

services:
  dashboard:
    build: .
    ports:
      - "8080:80"
    restart: unless-stopped
    environment:
      - NODE_ENV=production
    healthcheck:
      test: ["CMD", "wget", "--quiet", "--tries=1", "--spider", "http://localhost/health"]
      interval: 30s
      timeout: 3s
      retries: 3
```

Run:
```bash
docker-compose up -d
```

### Production Considerations

- Use secrets management for sensitive env vars
- Set up reverse proxy (nginx/Traefik) for HTTPS
- Enable logging and monitoring
- Configure backup strategy

---

## Self-Hosted Deployment

### Using Nginx

1. Build the application:
```bash
npm run build
```

2. Copy `dist/` to server:
```bash
rsync -avz dist/ user@server:/var/www/langa-dashboard/
```

3. Configure nginx (`/etc/nginx/sites-available/langa-dashboard`):

```nginx
server {
    listen 80;
    server_name dashboard.langa.io;
    root /var/www/langa-dashboard;
    index index.html;

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript text/xml application/xml application/xml+rss text/javascript;

    # Security headers
    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    # Cache static assets
    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # SPA routing
    location / {
        try_files $uri $uri/ /index.html;
    }
}
```

4. Enable site and reload:
```bash
sudo ln -s /etc/nginx/sites-available/langa-dashboard /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

5. Set up SSL with Let's Encrypt:
```bash
sudo certbot --nginx -d dashboard.langa.io
```

### Using Apache

1. Enable required modules:
```bash
sudo a2enmod rewrite headers deflate
```

2. Configure VirtualHost:

```apache
<VirtualHost *:80>
    ServerName dashboard.langa.io
    DocumentRoot /var/www/langa-dashboard

    <Directory /var/www/langa-dashboard>
        Options -Indexes +FollowSymLinks
        AllowOverride All
        Require all granted

        # SPA routing
        RewriteEngine On
        RewriteBase /
        RewriteRule ^index\.html$ - [L]
        RewriteCond %{REQUEST_FILENAME} !-f
        RewriteCond %{REQUEST_FILENAME} !-d
        RewriteRule . /index.html [L]
    </Directory>

    # Security headers
    Header always set X-Frame-Options "DENY"
    Header always set X-Content-Type-Options "nosniff"
    Header always set X-XSS-Protection "1; mode=block"

    # Compression
    DeflateCompressionLevel 6
    AddOutputFilterByType DEFLATE text/html text/plain text/xml text/css application/javascript application/json
</VirtualHost>
```

---

## Post-Deployment

### Verification Checklist

- [ ] Application loads without errors
- [ ] Login/registration works
- [ ] API connection successful
- [ ] All routes accessible
- [ ] Assets loading correctly
- [ ] HTTPS enabled (production)
- [ ] Security headers present
- [ ] Performance metrics acceptable

### Test Production Build

```bash
# Check bundle size
npm run build
du -sh dist/

# Test with production server
npm run preview

# Run Lighthouse audit
npx lighthouse http://localhost:4173 --view
```

### Monitoring

Set up monitoring for:
- Application uptime
- Error rates
- Performance metrics (Core Web Vitals)
- API response times
- User sessions

Recommended tools:
- **Vercel Analytics** (if using Vercel)
- **Google Analytics**
- **Sentry** for error tracking
- **LogRocket** for session replay

### Scaling

For high traffic:
- Enable CDN (Cloudflare, Vercel Edge Network)
- Implement rate limiting on API
- Set up load balancing
- Enable HTTP/2
- Optimize images and assets

### Rollback Strategy

Keep previous versions:
```bash
# Tag releases
git tag -a v1.0.0 -m "Release 1.0.0"
git push origin v1.0.0

# Rollback on Vercel
vercel rollback [deployment-url]

# Rollback with Docker
docker run previous-image-tag
```

---

## Troubleshooting

### Build Failures

```bash
# Clear cache and rebuild
rm -rf node_modules dist
npm ci
npm run build
```

### Runtime Errors

- Check browser console for errors
- Verify API endpoint accessibility
- Check CORS configuration
- Validate environment variables

### Performance Issues

- Check bundle size: `npm run build -- --stats`
- Analyze with webpack-bundle-analyzer
- Enable gzip/brotli compression
- Optimize images
- Implement code splitting

---

## Security Checklist

- [x] HTTPS enabled
- [x] Security headers configured
- [x] Environment variables secured
- [ ] API authentication working
- [ ] CORS properly configured
- [ ] CSP headers set (optional)
- [ ] Regular dependency updates
- [ ] Security audit passing

---

## Support

For deployment issues:
- Check [Documentation](../specs/001-react-dashboard/)
- Review [GitHub Issues](https://github.com/your-org/langa/issues)
- Contact: support@langa.io

---

**Last Updated**: January 2026
