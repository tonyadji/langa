# Changelog

All notable changes to the Langa Dashboard project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- **Production Docker Deployment**: Multi-stage Docker build with nginx serving
  - Platform-specific builds for `linux/amd64` architecture
  - Optimized production build with gzip compression and security headers
  - Health check endpoint at `/health`
  - Static asset caching with 1-year expiration
- **Environment Variable Support**: Build-time configuration for API endpoints
  - `VITE_API_BASE_URL` build argument for flexible backend URL configuration
  - Support for different environments (development, staging, production)
- **Nginx Reverse Proxy Configuration**: Complete VPS deployment setup
  - SSL/TLS support with wildcard certificates
  - HTTP to HTTPS redirection
  - Proxy headers for proper request forwarding
  - Production-grade security headers
- **Tailwind CSS v4 Integration**: Upgraded to latest Tailwind CSS
  - Added `@tailwindcss/vite` plugin - **CRITICAL for CSS compilation**
  - This plugin compiles raw Tailwind syntax into browser-compatible CSS
  - Without this plugin, browsers receive uncompiled Tailwind code and ignore it
  - PostCSS configuration for Tailwind v4
  - Enhanced styling capabilities
- **CI/CD Pipeline**: GitHub Actions workflow for automated VPS deployment
  - Docker image building and pushing
  - Platform-specific builds (linux/amd64)
  - Automated deployment to production server

### Changed
- **Docker Build Process**: Improved multi-stage build efficiency
  - Fixed npm installation command (`npm ci` instead of `--only=production=false`)
  - Added build arguments for environment-specific configuration
  - Optimized layer caching for faster rebuilds
- **Vite Configuration**: Enhanced production build settings
  - Set `base: '/'` for correct asset path resolution
  - Improved build optimization and minification
- **Production Build Size**: Optimized bundle sizes
  - Main CSS bundle: 43.10 kB (gzipped: 8.04 kB)
  - React vendor bundle: 31.78 kB (gzipped: 11.18 kB)
  - UI vendor bundle: 347.93 kB (gzipped: 101.65 kB)
  - Total JavaScript: ~620 kB (gzipped: ~180 kB)

### Fixed
- **CSS Rendering Issues**: Resolved missing styles in production
  - **Root Cause**: Missing Tailwind CSS compiler plugin caused Vite to ship raw Tailwind code (@theme{...}) directly to browsers
  - **Solution**: Added `@tailwindcss/vite` plugin to compile Tailwind CSS into browser-compatible standard CSS
  - Fixed nginx try_files syntax error ($ variable issue)
  - Ensured proper MIME types for CSS files
  - Fixed permissions for rootless Docker deployment
  - Set correct Vite base path configuration (`base: '/'`)
- **Platform Architecture Mismatch**: Fixed Docker deployment issues on VPS
  - Explicitly targeted `linux/amd64` platform for Intel/AMD servers
  - Prevented ARM64 image deployment on AMD64 architecture
- **Build Errors**: Resolved TypeScript compilation issues
  - Fixed missing `@tailwindcss/vite` dependency
  - Corrected npm installation flags in Dockerfile
- **API Connection**: Fixed backend API endpoint configuration
  - Properly configured VITE_API_BASE_URL for production builds
  - Ensured frontend connects to Railway backend in production

### Infrastructure
- **VPS Deployment**:
  - Server: 217.160.3.153 (auth-keycloak)
  - Domain: langa.capricedumardi.com
  - SSL: Wildcard certificate (*.capricedumardi.com)
  - Container Port: 3000
  - Exposed Ports: 80 (HTTP), 443 (HTTPS)
  - Firewall: Configured with firewall-cmd
- **Docker Registry**: ktac95/langa-dashboard on Docker Hub
- **Backend API**: https://langa-production.up.railway.app/api

### Security
- **HTTP Security Headers**: Added comprehensive security headers
  - X-Frame-Options: DENY
  - X-Content-Type-Options: nosniff
  - X-XSS-Protection: 1; mode=block
  - Referrer-Policy: strict-origin-when-cross-origin
- **SSL/TLS**: Production deployment with SSL certificates
  - TLS 1.2 and 1.3 support
  - Modern cipher suites
  - HTTP to HTTPS automatic redirection

### Performance
- **Compression**: Enabled gzip compression for text assets
  - Min length: 1024 bytes
  - Types: text/plain, text/css, text/xml, text/javascript, application/javascript, application/xml+rss, application/json
- **Caching**: Optimized cache headers for static assets
  - Assets: 1-year expiration with immutable flag
  - Proper cache-control headers
- **Build Optimization**: Improved build times
  - Multi-stage Docker builds for smaller images
  - Layer caching for dependencies
  - Optimized asset bundling

### Documentation
- Updated deployment guides with Docker and nginx configuration
- Added environment variable documentation
- Documented VPS deployment process
- Added troubleshooting section for common deployment issues

---

## [Previous Releases]

### Features Implemented (Before Deployment Updates)
- User authentication and authorization
- Application management (CRUD operations)
- Real-time log streaming and filtering
- Metrics visualization with charts
- Team management and collaboration
- Application sharing with granular permissions
- Usage monitoring and analytics
- Responsive UI with Tailwind CSS
- Comprehensive test coverage (80%+)
- Accessibility compliance (WCAG 2.1 AA)
- Performance optimization (FCP < 1.5s)
- TypeScript strict mode (zero errors)

---

## Release Notes

### Deployment v1.0.0 - 2026-01-17

This release marks the first production-ready deployment of the Langa Dashboard with complete Docker containerization, nginx configuration, and SSL support. The application is now fully deployable to VPS infrastructure with automated CI/CD pipelines.

**Key Highlights:**
- 🚀 Production-ready Docker deployment
- 🔒 SSL/TLS encryption with wildcard certificates
- ⚡ Optimized builds with ~180 KB gzipped JavaScript
- 🛡️ Comprehensive security headers
- 📦 Multi-stage builds for minimal image size
- 🔄 Automated CI/CD via GitHub Actions

**Migration Notes:**
- Ensure `VITE_API_BASE_URL` is set correctly for your environment
- Update nginx configuration on your VPS with proper SSL certificates
- Configure firewall to allow ports 80 and 443
- Pull the latest Docker image: `ktac95/langa-dashboard:latest`

**Known Issues:**
- Platform warnings in Docker builds (cosmetic, can be ignored)
- Browser may show warnings for self-signed certificates in development

**Next Steps:**
- Monitor application performance in production
- Set up logging and monitoring infrastructure
- Configure automated backups
- Implement CDN for static assets (optional)
