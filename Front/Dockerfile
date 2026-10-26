# =============================================================================
# ETAGE 1 : BUILD
# Compile l'application Angular en fichiers statiques.
# =============================================================================
FROM node:24-alpine AS build

WORKDIR /build

# package.json et package-lock.json d'abord : tant qu'ils ne changent pas,
# Docker reutilise la couche d'installation des dependances.
COPY package*.json ./
RUN npm ci

COPY . .

# Les tests sont executes par la pipeline CI.
RUN npm run build -- --configuration production


# =============================================================================
# ETAGE 2 : RUNTIME
# Nginx sert les fichiers statiques et relaie les appels API vers le backend.
# =============================================================================
FROM nginx:1.27-alpine AS runtime

COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=build /build/dist/pmt-frontend/browser /usr/share/nginx/html

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
