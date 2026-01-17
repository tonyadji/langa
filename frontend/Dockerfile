# Multi-stage build for production-ready Langa Dashboard
# Target Platform: linux/amd64 (Intel/AMD servers)

# Stage 1: Build the application
FROM --platform=linux/amd64 node:20-alpine AS builder

# Accept build arguments
ARG VITE_API_BASE_URL=https://langa-production.up.railway.app/api

WORKDIR /app

# Copy package files
COPY package*.json ./

# Install all dependencies (dev + production)
RUN npm ci && npm install -D terser

# Copy all configuration files
COPY tsconfig.json tsconfig.app.json tsconfig.node.json ./
COPY vite.config.ts ./
COPY index.html ./

# Copy source code and other necessary files
COPY src ./src
COPY public ./public

# Set environment variable for build
ENV VITE_API_BASE_URL=$VITE_API_BASE_URL

# Build the application with TypeScript type checking
RUN npm run build

# Stage 2: Serve with nginx
FROM --platform=linux/amd64 nginx:alpine

# Copy custom nginx config
COPY <<'EOF' /etc/nginx/conf.d/default.conf
server {
    listen 80;
    server_name localhost;
    root /usr/share/nginx/html;
    index index.html;

    gzip on;
    gzip_vary on;
    gzip_min_length 1024;
    gzip_types text/plain text/css text/xml text/javascript application/javascript application/xml+rss application/json;

    add_header X-Frame-Options "DENY" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-XSS-Protection "1; mode=block" always;
    add_header Referrer-Policy "strict-origin-when-cross-origin" always;

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        add_header X-Content-Type-Options "nosniff" always;
        # You can now use $uri safely without backslashes
        try_files $uri =404;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }

    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
}
EOF

# Copy built application from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# ADD THIS LINE:
# Ensure the nginx user owns the files
RUN chown -R nginx:nginx /usr/share/nginx/html && chmod -R 755 /usr/share/nginx/html

# Expose port 80
EXPOSE 80

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost/health || exit 1

# Start nginx
CMD ["nginx", "-g", "daemon off;"]
