# SmartLogi Delivery Management System (SDMS)

![Version](https://img.shields.io/badge/version-0.1.0-blue.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.7-brightgreen.svg)
![Java](https://img.shields.io/badge/Java-17-orange.svg)

## Description

SmartLogi est une application de gestion des livraisons développée avec Spring Boot pour suivre, planifier et optimiser l'envoi de colis à travers le Maroc.

## Fonctionnalités principales

- Gestion complète des clients expéditeurs et destinataires
- Suivi en temps réel des colis
- Planification des tournées de livraison par zone
- Historique complet de chaque colis
- Système de priorités pour les livraisons
- Gestion multi-produits par colis
- Notifications par email (bonus)

## Technologies utilisées

### Backend
- **Spring Boot 3.5.7** - Framework principal
- **Spring Data JPA** - Persistance des données
- **PostgreSQL** - Base de données
- **Liquibase** - Gestion des migrations de base de données
- **MapStruct 1.5.5** - Mapping Entity/DTO
- **Lombok** - Réduction du code boilerplate
- **SpringDoc OpenAPI** - Documentation API (Swagger)
- **Spring Validation** - Validation des données
- **Spring Mail** - Envoi d'emails
- **SLF4J** - Logging

### Outils
- **Maven** - Gestion de projet
- **Git/GitHub** - Versioning

## Prérequis

- Java 17 ou supérieur
- PostgreSQL 12 ou supérieur
- Maven 3.6+
- Git

## Installation

### 1. Cloner le repository

```bash
git clone https://github.com/votre-username/LogismartV2.git
cd LogismartV2
```

### 2. Configurer la base de données

Créer une base de données PostgreSQL :

```sql
CREATE DATABASE smartlogi_db;
```

### 3. Configurer l'application

Modifier le fichier `src/main/resources/application.yml` :

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/smartlogi_db
    username: votre_username
    password: votre_password
```

### 4. Compiler et lancer l'application

```bash
# Compiler le projet
./mvnw clean install

# Lancer l'application
./mvnw spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

## Documentation API

Une fois l'application lancée, la documentation Swagger est disponible à :

- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Docs** : http://localhost:8080/api-docs

## Architecture du projet

```
src/main/java/com/logismart/logismartv2/
├── config/          # Configuration Spring (OpenAPI, etc.)
├── controller/      # Contrôleurs REST
├── dto/             # Data Transfer Objects
├── entity/          # Entités JPA
├── exception/       # Gestion centralisée des exceptions
├── mapper/          # Interfaces MapStruct
├── repository/      # Repositories JPA
└── service/         # Logique métier

src/main/resources/
├── db/
│   └── changelog/   # Scripts Liquibase
└── application.yml  # Configuration de l'application
```

## Modèle de données

### Entités principales

- **ClientExpéditeur** - Clients qui envoient des colis
- **Destinataire** - Destinataires des colis
- **Livreur** - Livreurs assignés aux tournées
- **Colis** - Colis à livrer
- **Zone** - Zones géographiques de livraison
- **Produit** - Produits contenus dans les colis
- **HistoriqueLivraison** - Historique des statuts de colis

### Statuts de colis

- CREE
- COLLECTE
- EN_STOCK
- EN_TRANSIT
- LIVRE

### Priorités

- NORMALE
- URGENTE
- EXPRESS

## Endpoints API (à développer)

### Colis
- `GET /api/colis` - Liste des colis (avec pagination et filtres)
- `GET /api/colis/{id}` - Détails d'un colis
- `POST /api/colis` - Créer un colis
- `PUT /api/colis/{id}` - Modifier un colis
- `DELETE /api/colis/{id}` - Supprimer un colis

### Clients
- `GET /api/clients` - Liste des clients
- `POST /api/clients` - Créer un client

### Livreurs
- `GET /api/livreurs` - Liste des livreurs
- `GET /api/livreurs/{id}/colis` - Colis assignés à un livreur

*(... et plus encore)*

## Tests

```bash
# Lancer les tests
./mvnw test
```

## Contribution

1. Fork le projet
2. Créer une branche (`git checkout -b feature/AmazingFeature`)
3. Commit les changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la branche (`git push origin feature/AmazingFeature`)
5. Ouvrir une Pull Request

## Auteur

**Nafia Akdi** - Projet individuel Simplon

## Licence

Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.

## Roadmap

- [ ] Développer les entités JPA
- [ ] Créer les DTOs et Mappers
- [ ] Implémenter les repositories
- [ ] Développer les services métier
- [ ] Créer les contrôleurs REST
- [ ] Ajouter la pagination et les filtres
- [ ] Implémenter les recherches
- [ ] Ajouter les calculs statistiques
- [ ] Configurer l'envoi d'emails
- [ ] Créer les tests unitaires et d'intégration
- [ ] Déploiement

## Contact

Pour toute question : nafia.akdi@example.com
