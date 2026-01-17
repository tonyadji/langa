# Langa Dashboard

[![TypeScript](https://img.shields.io/badge/TypeScript-5.6-blue)](https://www.typescriptlang.org/)
[![React](https://img.shields.io/badge/React-18-blue)](https://react.dev/)
[![Vite](https://img.shields.io/badge/Vite-6-purple)](https://vitejs.dev/)
[![Tests](https://img.shields.io/badge/tests-passing-green)]()
[![Coverage](https://img.shields.io/badge/coverage->80%25-green)]()

A modern, responsive web application for managing and monitoring your Langa applications. Built with React 18, TypeScript, and Tailwind CSS.

## ✨ Features

### Implemented (Phases 1-9 + Polish)

- **🔐 Authentication & Authorization**
  - User registration and login
  - Session management with refresh tokens
  - Application creation and management
  - Role-based access control (Owner/Viewer)

- **📊 Application Management**
  - Create and manage applications
  - View application credentials
  - Toggle credential visibility
  - Copy-to-clipboard functionality

- **📋 Logs & Metrics**
  - Real-time log streaming
  - Advanced filtering (level, timeframe, search)
  - Metrics visualization with Recharts
  - Time-series data display

- **👥 Application Sharing**
  - Share with users and teams
  - Granular permission management
  - View shared users list
  - Revoke access

- **🏢 Team Management**
  - Create and manage teams
  - Invite members via email
  - Accept/decline invitations
  - Team-based application sharing

- **📈 Usage Monitoring**
  - Track log and metric bytes
  - Time-period filtering (7d/30d/90d)
  - Usage trend visualization
  - Storage breakdown

- **🎨 Quality & Polish**
  - Error boundaries
  - Accessibility (WCAG 2.1 AA)
  - Loading skeletons
  - Responsive design
  - TypeScript strict mode
  - **Dark mode** with system preference detection
  - Smooth theme transitions
  - Persistent theme storage

## � Prerequisites

- **Node.js** >= 18.0.0
- **npm** >= 9.0.0

Check your versions:
```bash
node -v  # Should be >= 18.0.0
npm -v   # Should be >= 9.0.0
```

## 🚀 Installation

### Clone and Setup

```bash
# Clone the repository
git clone <repository-url>
cd langa/frontend

# Install dependencies
npm install

# Set up environment variables
cp .env.example .env
# Edit .env with your configuration

# Start development server
npm run dev
```

Visit `http://localhost:5173` to see the app running.

## 💡 Quick Start

Once installed, you can:

1. **Create an Account**: Register a new user account
2. **Create an Application**: Set up your first application
3. **View Logs**: Monitor your application logs in real-time
4. **Analyze Metrics**: Track performance metrics
5. **Share Access**: Invite team members to collaborate

### Environment Variables

Copy `.env.example` to `.env` and configure:

- `VITE_API_BASE_URL`: Backend API URL (default: `http://localhost:8080`)
- `VITE_WS_URL`: WebSocket URL for real-time logs (default: `ws://localhost:8080/ws`)
- `VITE_ENV`: Environment (`development` or `production`)

See [.env.example](.env.example) for full documentation.

## 🧪 Examples

### API Integration

```typescript
import { authService } from '@/services/authService';
import { applicationService } from '@/services/applicationService';

// Login
await authService.login('user@example.com', 'password');

// Create application
const app = await applicationService.createApplication({
  name: 'My App',
  description: 'My application'
});
```

### Custom Hook Usage

```typescript
import { useApplications } from '@/features/applications/hooks/useApplications';

function MyComponent() {
  const { applications, loading, error } = useApplications();
  
  if (loading) return <div>Loading...</div>;
  if (error) return <div>Error: {error.message}</div>;
  
  return <div>{applications.length} applications</div>;
}
```

## 🔧 Troubleshooting

### Common Issues

**Port 5173 already in use**
```bash
# Kill the process using port 5173
lsof -ti:5173 | xargs kill -9

# Or use a different port
npm run dev -- --port 3000
```

**TypeScript errors after install**
```bash
# Clear cache and reinstall
rm -rf node_modules package-lock.json
npm install
```

**Tests failing with MSW errors**
```bash
# Update MSW handlers
npm run test -- --update
```

**Build fails with character encoding**
- Check for smart quotes or special characters in source files
- Use regular quotes and standard ASCII characters

**Cannot connect to backend**
- Verify `VITE_API_BASE_URL` in `.env`
- Ensure backend is running on the specified port
- Check CORS configuration on backend



## 📦 Tech Stack

- **Framework**: React 18 + TypeScript 5
- **Build**: Vite 6
- **Styling**: Tailwind CSS 3
- **Icons**: Lucide React
- **Charts**: Recharts 2
- **HTTP**: Axios
- **Testing**: Vitest + React Testing Library

## 🛠️ Development

### Scripts

```bash
npm run dev          # Start dev server
npm run build        # Build for production
npm run preview      # Preview production build
npm test             # Run tests
npm run test:coverage # Coverage report
npm run lint         # Run ESLint
npm run type-check   # TypeScript check
```

### Project Structure

```
src/
├── components/      # Reusable UI components
├── features/        # Feature modules
│   ├── applications/
│   ├── auth/
│   ├── logs/
│   ├── metrics/
│   ├── teams/
│   └── usage/
├── hooks/           # Custom hooks
├── pages/           # Page components
├── router/          # Routes
├── services/        # API services
└── types/           # TypeScript types
```

## ✅ Quality Standards

- **TypeScript**: Strict mode, zero errors
- **Testing**: Minimum 80% coverage
- **Accessibility**: WCAG 2.1 AA compliant
- **Performance**: FCP < 1.5s, LCP < 2.5s, TTI < 3s

## 🌐 Deployment

### Production Deployment with Docker

The application is containerized using a multi-stage Docker build optimized for production environments.

#### Prerequisites
- Docker 20.10+ with buildx support
- Access to a VPS or cloud server (AMD64/Intel architecture)
- Domain name with DNS configured (optional, for HTTPS)
- SSL certificate (optional, for HTTPS)

#### Quick Start

**1. Build the Docker image:**

```bash
# Build for production with Railway backend
docker buildx build \
  --platform linux/amd64 \
  --build-arg VITE_API_BASE_URL=https://langa-production.up.railway.app/api \
  -t langa-dashboard:latest \
  .

# Or build for custom backend
docker buildx build \
  --platform linux/amd64 \
  --build-arg VITE_API_BASE_URL=https://your-api.example.com/api \
  -t langa-dashboard:latest \
  .
```

**2. Run the container:**

```bash
docker run -d \
  --name langa-dashboard \
  -p 3000:80 \
  langa-dashboard:latest
```

**3. Access the application:**

```bash
# Visit http://localhost:3000
curl http://localhost:3000
```

#### Docker Compose Deployment

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

Deploy:

```bash
docker compose up -d
```

#### VPS Deployment with Nginx Reverse Proxy

**1. Install nginx on your VPS:**

```bash
sudo apt update
sudo apt install nginx -y
```

**2. Create nginx site configuration:**

```bash
sudo nano /etc/nginx/sites-available/langa-dashboard
```

Add the following configuration:

```nginx
# HTTP redirect to HTTPS
server {
    listen 80;
    server_name langa.yourdomain.com;
    return 301 https://$server_name$request_uri;
}

# HTTPS configuration
server {
    listen 443 ssl http2;
    server_name langa.yourdomain.com;

    # SSL certificates
    ssl_certificate /path/to/your/certificate.crt;
    ssl_certificate_key /path/to/your/private.key;

    # SSL configuration
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;

    # Proxy to Docker container
    location / {
        proxy_pass http://localhost:3000;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**3. Enable the site:**

```bash
sudo ln -s /etc/nginx/sites-available/langa-dashboard /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
```

**4. Configure firewall:**

```bash
sudo firewall-cmd --permanent --add-service=http
sudo firewall-cmd --permanent --add-service=https
sudo firewall-cmd --permanent --add-port=3000/tcp
sudo firewall-cmd --reload
```

**5. Deploy the application:**

```bash
docker pull ktac95/langa-dashboard:latest
docker compose up -d
```

#### Environment Configuration

The application supports build-time environment variables:

| Variable | Description | Default | Required |
|----------|-------------|---------|----------|
| `VITE_API_BASE_URL` | Backend API endpoint | `http://localhost:3000/api` | Yes |

Example `.env.production`:

```env
VITE_API_BASE_URL=https://langa-production.up.railway.app/api
VITE_APP_NAME=Langa Dashboard
VITE_ENABLE_DEBUG=false
```

#### CI/CD with GitHub Actions

The repository includes a GitHub Actions workflow for automated deployment:

**.github/workflows/deploy-vps.yml**

```yaml
name: Deploy to VPS

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Build and push Docker image
        uses: docker/build-push-action@v4
        with:
          context: ./frontend
          platforms: linux/amd64
          push: true
          tags: ktac95/langa-dashboard:latest
          build-args: |
            VITE_API_BASE_URL=https://langa-production.up.railway.app/api
```

#### Health Checks

The container exposes a health check endpoint:

```bash
curl http://localhost:3000/health
# Response: healthy
```

Docker health check:

```bash
docker inspect --format='{{.State.Health.Status}}' langa-dashboard
```

### Alternative Deployment Options

#### Vercel

```bash
npm i -g vercel
vercel --env VITE_API_BASE_URL=https://your-api.example.com/api
```

#### Netlify

```bash
npm run build
# Deploy dist/ directory via Netlify CLI or UI
```

#### Self-Hosted (Static Files)

```bash
npm run build
# Serve dist/ directory with any static file server
python -m http.server 8080 --directory dist
```

### Production Checklist

- [ ] Configure `VITE_API_BASE_URL` for production backend
- [ ] Set up SSL certificates (Let's Encrypt recommended)
- [ ] Configure nginx reverse proxy
- [ ] Enable firewall rules for ports 80 and 443
- [ ] Set up Docker health checks
- [ ] Configure automated backups
- [ ] Set up monitoring and logging
- [ ] Test HTTPS redirection
- [ ] Verify asset loading (CSS, JS, images)
- [ ] Test API connectivity from frontend

## 📚 Documentation

- **[Deployment Guide](./DEPLOYMENT.md)** - Complete production deployment instructions
- **[Deployment Quick Reference](./DEPLOYMENT-QUICKREF.md)** - Quick command reference
- **[Changelog](./CHANGELOG.md)** - Version history and release notes
- **[Specification](./documents/01-SPECIFICATION.md)** - Project requirements and architecture
- **[Tutorial](./documents/02-TUTORIAL.md)** - Getting started guide
- **[How-To Guides](./documents/03-HOW-TO.md)** - Task-oriented recipes
- **[Reference](./documents/04-REFERENCE.md)** - API and component reference
- **[Explanation](./documents/05-EXPLANATION.md)** - Architecture and design decisions

## 🤝 Contributing

1. Fork the repository
2. Create feature branch
3. Write tests first (TDD)
4. Implement feature
5. Ensure tests pass
6. Submit Pull Request

## 📝 License

MIT

## 💬 Support

- Issues: [GitHub Issues](https://github.com/your-org/langa/issues)
- Docs: [docs.langa.io](https://docs.langa.io)
- Email: support@langa.io

---

Built with ❤️ by the Langa Team
