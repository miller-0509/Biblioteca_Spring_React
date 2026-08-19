# ── Etapa 1: build de React + Vite ─────────────────────────────────────
FROM node:22-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm ci

COPY . .
RUN npm run build

# ── Etapa 2: nginx (SPA + proxy /api) ──────────────────────────────────
FROM nginx:1.27-alpine

# Plantilla: docker-entrypoint sustituye ${BACKEND_URL} en el arranque
COPY nginx.conf /etc/nginx/templates/default.conf.template
COPY --from=build /app/dist /usr/share/nginx/html

ENV BACKEND_URL=http://localhost:8080

EXPOSE 80