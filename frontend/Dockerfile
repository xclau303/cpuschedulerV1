# Stage 1: Build the Vite app
FROM node:18-alpine AS builder

# Install build dependencies
RUN apk add --no-cache python3 make g++

# Set working directory
WORKDIR /app

# Copy package files and install dependencies
COPY package*.json ./
RUN npm ci --legacy-peer-deps

# Copy app source code
COPY . .

COPY .env.production ./
# Set environment variable for API endpoint (adjust for container-internal backend access)
#ENV VITE_API_URL=http://backend:8085 commented out  for now

# Build the Vite app
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:stable-alpine

# Clean default Nginx static files
RUN rm -rf /usr/share/nginx/html/*

# Copy built files from builder stage
COPY --from=builder /app/dist /usr/share/nginx/html

# Optional: copy custom Nginx config for React routing
COPY nginx.conf /etc/nginx/nginx.conf

# Expose HTTP port
EXPOSE 80

# Start Nginx
CMD ["nginx", "-g", "daemon off;"]
