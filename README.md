# 🚚 SmartLogi Delivery Management System (SDMS) - v0.2.0

> Application de gestion logistique complète avec authentification JWT, autorisation basée sur les rôles et gestion dynamique des permissions.

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://openjdk.org/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Ready-2496ED.svg)](https://www.docker.com/)

---

## 📋 Table des matières

- [Aperçu](#-aperçu)
- [Fonctionnalités clés](#-fonctionnalités-clés)
- [Architecture](#-architecture)
- [Installation rapide](#-installation-rapide)
  - [Avec Docker (Recommandé)](#avec-docker-recommandé)
  - [Manuel](#manuel)
- [Utilisation](#-utilisation)
- [Rôles et permissions](#-rôles-et-permissions)
- [Endpoints API](#-endpoints-api)
- [Documentation](#-documentation)
- [Sécurité](#-sécurité)
- [Dépannage](#-dépannage)

---

## 🎯 Aperçu

SmartLogi SDMS est un système de gestion de livraison moderne conçu pour gérer :
- 📦 **Colis** : Création, suivi, assignation, historique
- 🗺️ **Zones de livraison** : Gestion géographique
- 🚴 **Livreurs** : Affectation, suivi des performances
- 👥 **Clients** : Expéditeurs et destinataires
- 📊 **Statistiques** : Tableaux de bord et analytics
- 🔐 **Sécurité** : JWT + Permissions dynamiques

---

## ✨ Fonctionnalités clés

### 🔒 Sécurité avancée
- ✅ **JWT Stateless** : Authentification sans session
- ✅ **Autorisation multi-niveaux** : Rôles + Permissions dynamiques
- ✅ **Chiffrement BCrypt** : Stockage sécurisé des mots de passe
- ✅ **CORS configuré** : Support multi-frontend
- ✅ **Admin système** : Gestion des permissions en temps réel

### 📱 API RESTful complète
- ✅ **9 ressources métier** : Parcels, Zones, Delivery Persons, etc.
- ✅ **100+ endpoints** : CRUD complet + fonctionnalités avancées
- ✅ **Swagger UI** : Documentation interactive
- ✅ **Validation** : Bean Validation (JSR-380)

### 🐳 Déploiement simplifié
- ✅ **Docker multi-stage** : Image optimisée (~250MB)
- ✅ **Docker Compose** : PostgreSQL + API en 1 commande
- ✅ **Scripts automatisés** : Démarrage/arrêt/nettoyage
- ✅ **Health checks** : Monitoring intégré

### 📊 Gestion des données
- ✅ **PostgreSQL 16** : Base de données robuste
- ✅ **Liquibase** : Migrations versionnées
- ✅ **Hibernate** : ORM puissant
- ✅ **Données de test** : Jeu de données initial

---

## 🏗️ Architecture

### Architecture multi-modules Maven

```
Logismart-V2/
├── logismart-security/     # Module sécurité (JWT, auth, permissions)
│   ├── entity/             # User, Permission
│   ├── service/            # JwtService, AuthService, PermissionService
│   ├── controller/         # AuthController, PermissionController (Admin)
│   ├── filter/             # JwtAuthenticationFilter
│   └── config/             # SecurityConfig, CorsConfig
│
├── logismart-api/          # Module métier
│   ├── controller/         # 9 contrôleurs REST
│   ├── service/            # Logique métier
│   ├── repository/         # Accès données (JPA)
│   ├── entity/             # Parcel, Zone, DeliveryPerson, etc.
│   └── dto/                # Request/Response DTOs
│
├── Dockerfile              # Image Docker multi-stage
├── docker-compose.yml      # Orchestration PostgreSQL + API
└── scripts/                # Scripts de démarrage
```

### Flux d'authentification

```
┌─────────┐                  ┌────────────┐                  ┌──────────┐
│ Client  │                  │  Backend   │                  │    DB    │
└────┬────┘                  └─────┬──────┘                  └────┬─────┘
     │ POST /auth/login            │                              │
     │ {username, password}        │                              │
     ├────────────────────────────>│ 1. Validate credentials      │
     │                              ├─────────────────────────────>│
     │                              │ 2. Generate JWT (24h)        │
     │ {token, role, permissions}  │                              │
     │<────────────────────────────┤                              │
     │                              │                              │
     │ GET /api/parcels/my-parcels │                              │
     │ Authorization: Bearer {JWT} │                              │
     ├────────────────────────────>│ 3. Validate JWT              │
     │                              │ 4. Check permissions         │
     │                              │ 5. Execute query             │
     │                              ├─────────────────────────────>│
     │ 200 OK {data: [...]}        │<─────────────────────────────┤
     │<────────────────────────────┤                              │
```

---

## 🚀 Installation rapide

### Avec Docker (Recommandé)

**Prérequis:** Docker + Docker Compose

```bash
# 1. Cloner le projet
git clone https://github.com/votre-username/Logismart-V2.git
cd Logismart-V2

# 2. Configurer l'environnement
cp .env.example .env
nano .env  # Remplir DB_PASSWORD et JWT_SECRET

# 3. Démarrer (tout-en-un)
./scripts/start-docker.sh  # ou start-docker.bat sur Windows

# 4. Accéder à l'application
# API: http://localhost:8080
# Swagger: http://localhost:8080/swagger-ui.html
```

**Variables requises (.env):**
```bash
DB_PASSWORD=your_secure_password
JWT_SECRET=your_jwt_secret_min_256_bits  # Générer: openssl rand -base64 64
```

### Manuel

**Prérequis:** JDK 17+, Maven 3.9+, PostgreSQL 16+

```bash
# 1. Créer la base de données
createdb Logismart-V2

# 2. Compiler
mvn clean install

# 3. Configurer application.yml
# spring.datasource.url=jdbc:postgresql://localhost:5432/Logismart-V2
# spring.datasource.username=postgres
# spring.datasource.password=votre_mot_de_passe
# jwt.secret=votre_secret_256_bits_minimum

# 4. Démarrer
cd logismart-api
mvn spring-boot:run

# 5. Vérifier
curl http://localhost:8080/actuator/health
```

---

## 💻 Utilisation

### 1. Authentification

```bash
# Login Manager
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "manager",
    "password": "manager123"
  }'

# Réponse:
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "role": "ROLE_MANAGER",
  "username": "manager"
}
```

### 2. Utiliser le token

```bash
# Sauvegarder le token
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

# Requête authentifiée
curl -X GET http://localhost:8080/api/parcels \
  -H "Authorization: Bearer $TOKEN"
```

### 3. Comptes par défaut

| Username | Password | Rôle | Description |
|----------|----------|------|-------------|
| **admin** | admin123 | ROLE_ADMIN | Admin système + gestion permissions |
| manager | manager123 | ROLE_MANAGER | Gestionnaire opérations |
| livreur | livreur123 | ROLE_LIVREUR | Livreur (1 colis assigné) |
| client | client123 | ROLE_CLIENT | Client (1 colis créé) |

### 4. Exemples complets par rôle

#### 🔴 ADMIN - Gérer les permissions

```bash
# 1. Login admin
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'

# Réponse: {"token":"eyJ...","role":"ROLE_ADMIN","username":"admin"}

# 2. Lister toutes les permissions disponibles
curl -X GET http://localhost:8080/api/admin/permissions \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 3. Créer une nouvelle permission
curl -X POST http://localhost:8080/api/admin/permissions \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "REPORT_EXPORT",
    "description": "Permet d'\''exporter les rapports",
    "enabled": true
  }'

# 4. Assigner une permission à un utilisateur
curl -X POST http://localhost:8080/api/admin/users/client-uuid/permissions/perm-uuid \
  -H "Authorization: Bearer $ADMIN_TOKEN"

# 5. Voir les permissions d'un utilisateur
curl -X GET http://localhost:8080/api/admin/users/client-uuid/permissions \
  -H "Authorization: Bearer $ADMIN_TOKEN"
```

#### 🟢 MANAGER - Workflow complet de livraison

```bash
# 1. Login manager
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"manager","password":"manager123"}'

MANAGER_TOKEN="eyJhbGciOi..."

# 2. Créer une zone de livraison
curl -X POST http://localhost:8080/api/zones \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Casablanca Centre",
    "description": "Zone centre-ville"
  }'

# Réponse: {"id":"zone-uuid","name":"Casablanca Centre",...}

# 3. Créer un livreur et l'assigner à la zone
curl -X POST http://localhost:8080/api/delivery-persons \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ahmed",
    "phone": "0612345678",
    "zoneId": "zone-uuid"
  }'

# 4. Créer un client expéditeur
curl -X POST http://localhost:8080/api/sender-clients \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "E-Shop Maroc",
    "phone": "0523456789",
    "address": "Bd Mohammed V, Casablanca"
  }'

# 5. Créer un destinataire
curl -X POST http://localhost:8080/api/recipients \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Fatima Zahra",
    "phone": "0676543210",
    "address": "Rue des FAR, Casablanca"
  }'

# 6. Créer un colis
curl -X POST http://localhost:8080/api/parcels \
  -H "Authorization: Bearer $MANAGER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Smartphone Galaxy S24",
    "weight": 0.5,
    "priority": "EXPRESS",
    "senderId": "sender-uuid",
    "recipientId": "recipient-uuid",
    "deliveryPersonId": "delivery-person-uuid"
  }'

# 7. Voir tous les colis (avec pagination)
curl -X GET "http://localhost:8080/api/parcels?page=0&size=20&sort=createdAt,desc" \
  -H "Authorization: Bearer $MANAGER_TOKEN"

# 8. Filtrer les colis par statut
curl -X GET http://localhost:8080/api/parcels/status/IN_TRANSIT \
  -H "Authorization: Bearer $MANAGER_TOKEN"

# 9. Voir le dashboard de statistiques
curl -X GET http://localhost:8080/api/statistics/dashboard \
  -H "Authorization: Bearer $MANAGER_TOKEN"

# Réponse: {
#   "totalParcels": 150,
#   "deliveredParcels": 120,
#   "inTransitParcels": 25,
#   "deliveryRate": 80.0,
#   ...
# }
```

#### 🟡 LIVREUR - Gérer mes livraisons

```bash
# 1. Login livreur
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"livreur","password":"livreur123"}'

LIVREUR_TOKEN="eyJhbGciOi..."

# 2. Voir mes colis assignés
curl -X GET http://localhost:8080/api/parcels/my-parcels \
  -H "Authorization: Bearer $LIVREUR_TOKEN"

# Réponse: [
#   {
#     "id": "parcel-uuid",
#     "description": "Smartphone Galaxy S24",
#     "status": "COLLECTED",
#     "priority": "EXPRESS",
#     "recipient": {...}
#   }
# ]

# 3. Commencer une livraison (statut → IN_TRANSIT)
curl -X PUT "http://localhost:8080/api/parcels/parcel-uuid/status?status=IN_TRANSIT" \
  -H "Authorization: Bearer $LIVREUR_TOKEN"

# Réponse: {"id":"parcel-uuid","status":"IN_TRANSIT",...}

# 4. Confirmer la livraison (statut → DELIVERED)
curl -X PUT "http://localhost:8080/api/parcels/parcel-uuid/status?status=DELIVERED" \
  -H "Authorization: Bearer $LIVREUR_TOKEN"

# 5. Voir mon historique de livraisons
curl -X GET http://localhost:8080/api/delivery-history/my-history \
  -H "Authorization: Bearer $LIVREUR_TOKEN"

# 6. Suivre un colis spécifique
curl -X GET http://localhost:8080/api/parcels/parcel-uuid/tracking \
  -H "Authorization: Bearer $LIVREUR_TOKEN"

# ❌ Tentative d'accès aux zones (refusé)
curl -X GET http://localhost:8080/api/zones \
  -H "Authorization: Bearer $LIVREUR_TOKEN"
# Réponse: 403 Forbidden
```

#### 🔵 CLIENT - Suivre mes envois

```bash
# 1. Login client
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"client","password":"client123"}'

CLIENT_TOKEN="eyJhbGciOi..."

# 2. Créer une demande de livraison
curl -X POST http://localhost:8080/api/parcels \
  -H "Authorization: Bearer $CLIENT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "description": "Laptop HP EliteBook",
    "weight": 2.5,
    "priority": "NORMAL",
    "recipientId": "recipient-uuid"
  }'

# Réponse: {
#   "id": "new-parcel-uuid",
#   "description": "Laptop HP EliteBook",
#   "status": "CREATED",
#   "trackingNumber": "TRK-2024-001234"
# }

# 3. Voir mes colis uniquement
curl -X GET http://localhost:8080/api/parcels/my-parcels \
  -H "Authorization: Bearer $CLIENT_TOKEN"

# 4. Voir les détails d'un de mes colis
curl -X GET http://localhost:8080/api/parcels/new-parcel-uuid \
  -H "Authorization: Bearer $CLIENT_TOKEN"

# 5. Suivre l'état de livraison en temps réel
curl -X GET http://localhost:8080/api/parcels/new-parcel-uuid/tracking \
  -H "Authorization: Bearer $CLIENT_TOKEN"

# Réponse: {
#   "parcelId": "new-parcel-uuid",
#   "currentStatus": "IN_TRANSIT",
#   "estimatedDelivery": "2024-12-18T15:00:00",
#   "statusHistory": [
#     {"status":"CREATED","timestamp":"2024-12-17T09:00:00"},
#     {"status":"COLLECTED","timestamp":"2024-12-17T10:30:00"},
#     {"status":"IN_TRANSIT","timestamp":"2024-12-17T14:00:00"}
#   ]
# }

# 6. Voir l'historique complet de mes livraisons
curl -X GET http://localhost:8080/api/parcels/new-parcel-uuid/history \
  -H "Authorization: Bearer $CLIENT_TOKEN"

# ❌ Tentative de voir tous les colis (refusé)
curl -X GET http://localhost:8080/api/parcels \
  -H "Authorization: Bearer $CLIENT_TOKEN"
# Réponse: 403 Forbidden
```

---

## 👥 Rôles et permissions

### 🔴 ROLE_ADMIN (Administrateur système)

**Accès : Gestion complète du système**

| Ressource | Permissions |
|-----------|-------------|
| Permissions | ✅ CRUD permissions (créer, modifier, supprimer) |
| Utilisateurs | ✅ Assigner/révoquer permissions aux users |
| Système | ✅ Tous les accès ROLE_MANAGER |

**Endpoints spécifiques:**
- `POST /api/admin/permissions` - Créer une permission
- `GET /api/admin/permissions` - Lister toutes les permissions
- `PUT /api/admin/permissions/{id}` - Modifier une permission
- `DELETE /api/admin/permissions/{id}` - Supprimer une permission
- `POST /api/admin/users/{userId}/permissions/{permissionId}` - Assigner permission
- `DELETE /api/admin/users/{userId}/permissions/{permissionId}` - Révoquer permission
- `GET /api/admin/users/{userId}/permissions` - Voir permissions d'un user

### 🟢 ROLE_MANAGER (Gestionnaire)

**Accès : Complet sur toutes les opérations**

| Ressource | Permissions |
|-----------|-------------|
| Colis | ✅ CRUD complet (tous les colis) |
| Zones | ✅ CRUD complet |
| Livreurs | ✅ CRUD complet |
| Clients | ✅ CRUD complet |
| Statistiques | ✅ Accès complet |

**Endpoints:** ~100 endpoints disponibles

### 🟡 ROLE_LIVREUR (Livreur)

**Accès : Limité aux opérations de livraison**

| Ressource | Permissions |
|-----------|-------------|
| Mes colis | ✅ Voir uniquement mes colis assignés |
| Statut | ✅ Mettre à jour le statut (IN_TRANSIT, DELIVERED) |
| Historique | ✅ Mon historique de livraisons |

**Endpoints spécifiques:**
- `GET /api/parcels/my-parcels` - Mes colis assignés
- `PUT /api/parcels/{id}/status?status=IN_TRANSIT` - Mettre à jour statut
- `GET /api/delivery-history/my-history` - Mon historique
- `GET /api/parcels/{id}/tracking` - Suivi de mes colis

**Restrictions:**
- ❌ Pas d'accès aux colis des autres livreurs
- ❌ Pas d'accès aux zones, clients, stats

### 🔵 ROLE_CLIENT (Client expéditeur)

**Accès : Limité à ses propres envois**

| Ressource | Permissions |
|-----------|-------------|
| Mes colis | ✅ Créer des demandes de livraison |
| Mes colis | ✅ Voir uniquement mes colis |
| Suivi | ✅ Suivre mes envois |

**Endpoints spécifiques:**
- `POST /api/parcels` - Créer une demande de livraison
- `GET /api/parcels/my-parcels` - Voir mes colis
- `GET /api/parcels/{id}` - Détails d'un de mes colis
- `GET /api/parcels/{id}/tracking` - Suivre un colis
- `GET /api/parcels/{id}/history` - Historique d'un colis

**Restrictions:**
- ❌ Pas d'accès aux colis des autres clients
- ❌ Pas d'accès aux livreurs, zones, stats

---

## 📡 Endpoints API

### Authentification (Public)

```bash
POST /auth/login                    # Login (retourne JWT)
```

### Parcels

```bash
# Manager
GET    /api/parcels                 # Tous les colis (paginé)
POST   /api/parcels                 # Créer un colis
GET    /api/parcels/{id}            # Détails colis
PUT    /api/parcels/{id}            # Modifier colis
DELETE /api/parcels/{id}            # Supprimer colis
GET    /api/parcels/search          # Recherche avancée
GET    /api/parcels/status/{status} # Filtrer par statut
GET    /api/parcels/unassigned      # Colis non assignés

# Livreur / Client
GET    /api/parcels/my-parcels      # Mes colis (rôle-basé)
PUT    /api/parcels/{id}/status     # Mettre à jour statut (livreur)
GET    /api/parcels/{id}/tracking   # Suivi colis (client + livreur)
GET    /api/parcels/{id}/history    # Historique colis
```

### Zones (Manager uniquement)

```bash
GET    /api/zones                   # Toutes les zones
POST   /api/zones                   # Créer zone
GET    /api/zones/{id}              # Détails zone
PUT    /api/zones/{id}              # Modifier zone
DELETE /api/zones/{id}              # Supprimer zone
```

### Delivery Persons (Manager uniquement)

```bash
GET    /api/delivery-persons        # Tous les livreurs
POST   /api/delivery-persons        # Créer livreur
GET    /api/delivery-persons/{id}   # Détails livreur
PUT    /api/delivery-persons/{id}   # Modifier livreur
DELETE /api/delivery-persons/{id}   # Supprimer livreur
```

### Delivery History

```bash
# Manager
GET    /api/delivery-history                    # Tout l'historique
GET    /api/delivery-history/parcel/{parcelId}  # Historique colis

# Livreur
GET    /api/delivery-history/my-history         # Mon historique
```

### Statistics (Manager uniquement)

```bash
GET    /api/statistics/dashboard     # Dashboard complet
GET    /api/statistics/parcels       # Stats colis
GET    /api/statistics/deliveries    # Stats livraisons
```

### Admin - Permissions (Admin uniquement)

```bash
# Gestion permissions
GET    /api/admin/permissions                   # Toutes les permissions
POST   /api/admin/permissions                   # Créer permission
GET    /api/admin/permissions/{id}              # Détails permission
PUT    /api/admin/permissions/{id}              # Modifier permission
DELETE /api/admin/permissions/{id}              # Supprimer permission
GET    /api/admin/permissions/search?keyword=  # Rechercher

# Assignation permissions
POST   /api/admin/users/{userId}/permissions/{permissionId}  # Assigner
DELETE /api/admin/users/{userId}/permissions/{permissionId}  # Révoquer
GET    /api/admin/users/{userId}/permissions                # Lister
```

---

## 📚 Documentation

### Swagger UI (Interactive)

```
http://localhost:8080/swagger-ui.html
```

**Authentification dans Swagger:**
1. Cliquer sur "Authorize" 🔓
2. Entrer: `Bearer YOUR_JWT_TOKEN`
3. Tester les endpoints directement

### OpenAPI JSON

```
http://localhost:8080/v3/api-docs
```

### Postman Collection

Importer `Logismart-SDMS.postman_collection.json` dans Postman.

---

## 🔐 Sécurité

### JWT Configuration

**Algorithme:** HMAC-SHA384
**Expiration:** 24 heures
**Claims:** sub (username), roles, permissions, iat, exp

**Générer un secret sécurisé:**
```bash
openssl rand -base64 64
```

### CORS

**Origines autorisées:**
- http://localhost:3000 (React)
- http://localhost:4200 (Angular)
- http://localhost:8080 (Backend)

**Méthodes:** GET, POST, PUT, PATCH, DELETE, OPTIONS
**Headers:** Authorization, Content-Type, Accept

### Permissions dynamiques

Le système combine **rôles fixes** (ADMIN, MANAGER, LIVREUR, CLIENT) avec **permissions dynamiques** assignables par l'admin:

**Permissions par défaut:**
- `PARCEL_CREATE`, `PARCEL_READ`, `PARCEL_UPDATE`, `PARCEL_DELETE`
- `ZONE_MANAGE`
- `DELIVERY_PERSON_MANAGE`
- `STATISTICS_VIEW`
- `PERMISSION_MANAGE` (admin uniquement)

**Exemple d'assignation:**
```bash
# Donner permission PARCEL_CREATE au user 'client-001'
POST /api/admin/users/client-001/permissions/perm-parcel-create
Authorization: Bearer {admin_token}
```

---

## 🐛 Dépannage

### Problème: "Bad credentials"
✅ Vérifiez username/password. Les mots de passe sont case-sensitive.

### Problème: "Token expired"
✅ Reconnectez-vous (`POST /auth/login`) pour obtenir un nouveau token.

### Problème: "403 Forbidden"
✅ Votre rôle n'a pas accès à cet endpoint. Vérifiez les permissions.

### Problème: "401 Unauthorized"
✅ Token manquant ou invalide. Vérifiez le header `Authorization: Bearer {token}`.

### Problème: CORS error
✅ Vérifiez que votre frontend tourne sur un port autorisé (3000, 4200, 8080).

### Problème: Docker - Base de données non accessible
✅ Vérifiez que `.env` contient `DB_PASSWORD`. Relancez: `docker-compose down && docker-compose up`.

### Logs Docker

```bash
# Voir les logs
docker-compose logs -f logismart-api

# Voir les logs PostgreSQL
docker-compose logs -f postgres

# Statut des containers
docker-compose ps
```

---

## 🧪 Tests

### Tests manuels (cURL)

```bash
# ✅ Login réussi
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"manager","password":"manager123"}'

# ✅ Accès autorisé (Manager → Zones)
curl -X GET http://localhost:8080/api/zones \
  -H "Authorization: Bearer {token}"

# ❌ Accès refusé (Client → Zones)
curl -X GET http://localhost:8080/api/zones \
  -H "Authorization: Bearer {client_token}"
# Attendu: 403 Forbidden

# ❌ Token invalide
curl -X GET http://localhost:8080/api/zones \
  -H "Authorization: Bearer invalid_token"
# Attendu: 401 Unauthorized
```

### Collection Postman

26 scénarios de test couvrant :
- Authentification (7 tests)
- Autorisations MANAGER (5 tests)
- Autorisations LIVREUR (7 tests)
- Autorisations CLIENT (7 tests)

**Importer:** `Logismart-SDMS.postman_collection.json`

---

## 🐳 Docker Commands

```bash
# Démarrer
./scripts/start-docker.sh

# Arrêter (garde les données)
./scripts/stop-docker.sh

# Nettoyer (supprime tout)
./scripts/clean-docker.sh

# Voir les logs en temps réel
docker-compose logs -f

# Redémarrer un service
docker-compose restart logismart-api

# Rebuild et redémarrer
docker-compose up --build -d
```

---

## 📊 Technologies

| Catégorie | Technologie | Version |
|-----------|-------------|---------|
| **Backend** | Spring Boot | 3.3.5 |
| **Sécurité** | Spring Security | 6.3.4 |
| **JWT** | JJWT | 0.12.6 |
| **Base de données** | PostgreSQL | 16 |
| **ORM** | Hibernate | 6.5.3 |
| **Migrations** | Liquibase | 4.29.2 |
| **Documentation** | SpringDoc OpenAPI | 2.6.0 |
| **Conteneurisation** | Docker | Latest |
| **Build** | Maven | 3.9+ |
| **Java** | OpenJDK | 17 |

---

## 🤝 Contribution

1. Fork le projet
2. Créer une branche feature (`git checkout -b feature/AmazingFeature`)
3. Commit (`git commit -m 'Add AmazingFeature'`)
4. Push (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

---

## 📝 License

Projet développé dans le cadre de la formation **Simplon - Javalution**.

---

## 👤 Auteur

**Votre Nom**
📅 17/11/2024 - 19/12/2024
🎯 Brief: Sécurisation JWT + Permissions dynamiques + Docker

---

## 🎉 Projet terminé avec succès !

✅ Authentification JWT stateless
✅ Autorisation multi-niveaux (Rôles + Permissions)
✅ Gestion dynamique des permissions (Admin)
✅ Endpoints role-based (Manager, Livreur, Client)
✅ CORS sécurisé
✅ Docker prêt pour production
✅ Documentation complète
✅ Tests Postman

**🚀 Prêt pour déploiement !**
