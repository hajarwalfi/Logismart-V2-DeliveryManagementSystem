# 📋 Guide de Test API - STRICTEMENT CONFORME AU BRIEF

**Version:** 0.1.0
**Date:** 07 Novembre 2025
**Objectif:** Tester UNIQUEMENT les 15 user stories du brief, sans rien ajouter

---

## 🎯 Les 15 User Stories du Brief

### Client Expéditeur (3 US)
1. ✅ **US-1:** Créer une demande de livraison
2. ✅ **US-2:** Consulter mes colis en cours et livrés
3. ❌ **US-3:** Recevoir notification email (BONUS - non implémenté)

### Destinataire (1 US)
4. ✅ **US-4:** Consulter le statut de mes colis

### Livreur (2 US)
5. ✅ **US-5:** Voir mes colis assignés avec priorités et zones
6. ✅ **US-6:** Mettre à jour le statut des colis

### Gestionnaire Logistique (9 US)
7. ✅ **US-7:** Voir demandes et assigner aux livreurs
8. ✅ **US-8:** Corriger ou supprimer informations erronées
9. ✅ **US-9:** Filtrer et paginer par statut, zone, ville, priorité, date
10. ✅ **US-10:** Regrouper par zone, statut, priorité
11. ✅ **US-11:** Rechercher par mot-clé (nom, numéro, ville)
12. ✅ **US-12:** Calculer poids total et nombre par livreur/zone
13. ⚠️ **US-13:** Identifier colis en retard/prioritaires (alerte email non implémentée)
14. ✅ **US-14:** Associer plusieurs produits à un colis
15. ✅ **US-15:** Consulter historique complet d'un colis

---

## 🔧 PRÉREQUIS MINIMAUX (Hors brief, mais nécessaires)

Ces données doivent exister AVANT de tester les user stories.

### 1. Créer une Zone de Livraison
```http
POST http://localhost:8080/api/zones
Content-Type: application/json

{
  "name": "Zone Casablanca Centre",
  "description": "Zone couvrant le centre-ville de Casablanca",
  "postalCode": "20000",
  "city": "Casablanca"
}
```
**Sauvegarder:** `{zone_id}`

---

### 2. Créer un Produit
```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "name": "Smartphone Samsung Galaxy",
  "category": "Electronics",
  "weight": 0.5,
  "price": 2999.99
}
```
**Sauvegarder:** `{product_id}`

---

### 3. Créer un Livreur
```http
POST http://localhost:8080/api/delivery-persons
Content-Type: application/json

{
  "firstName": "Ahmed",
  "lastName": "Bennani",
  "phone": "+212600000001",
  "vehicle": "Scooter Yamaha 125",
  "assignedZoneId": "{zone_id}"
}
```
**Sauvegarder:** `{delivery_person_id}`

---

### 4. Créer un Client Expéditeur
```http
POST http://localhost:8080/api/sender-clients
Content-Type: application/json

{
  "firstName": "Fatima",
  "lastName": "Alami",
  "email": "fatima.alami@email.com",
  "phone": "+212611111111",
  "address": "123 Rue Mohammed V, Casablanca"
}
```
**Sauvegarder:** `{sender_client_id}`

---

### 5. Créer un Destinataire
```http
POST http://localhost:8080/api/recipients
Content-Type: application/json

{
  "firstName": "Youssef",
  "lastName": "Idrissi",
  "email": "youssef.idrissi@email.com",
  "phone": "+212622222222",
  "address": "456 Avenue Hassan II, Casablanca"
}
```
**Sauvegarder:** `{recipient_id}`

---

## 👤 CLIENT EXPÉDITEUR - Tests des User Stories

### ✅ US-1: Créer une Demande de Livraison

**Story du Brief:** "Je veux créer une demande de livraison pour envoyer un colis à un destinataire."

```http
POST http://localhost:8080/api/parcels
Content-Type: application/json

{
  "senderClientId": "{sender_client_id}",
  "recipientId": "{recipient_id}",
  "description": "Smartphone Samsung Galaxy neuf",
  "weight": 0.5,
  "priority": "NORMAL",
  "destinationCity": "Casablanca",
  "destinationAddress": "456 Avenue Hassan II, Casablanca",
  "products": [
    {
      "productId": "{product_id}",
      "quantity": 1
    }
  ]
}
```

**Attendu:** 201 Created avec le colis créé
**Sauvegarder:** `{parcel_id}`

---

### ✅ US-2: Consulter Mes Colis En Cours et Livrés

**Story du Brief:** "Je veux consulter la liste de mes colis en cours et livrés pour suivre mes envois."

#### Test 2A: Colis En Cours
```http
GET http://localhost:8080/api/sender-clients/{sender_client_id}/parcels/in-progress
```
**Attendu:** 200 OK avec colis ayant statut: CREATED, COLLECTED, IN_STOCK, IN_TRANSIT

#### Test 2B: Colis Livrés
```http
GET http://localhost:8080/api/sender-clients/{sender_client_id}/parcels/delivered
```
**Attendu:** 200 OK avec colis ayant statut: DELIVERED

---

### ❌ US-3: Recevoir Notification Email (BONUS)

**Story du Brief:** "Je veux recevoir une notification par email lorsque mon colis est collecté ou livré."

**Status:** NON IMPLÉMENTÉ - Le brief marque SMTP comme (bonus)

---

## 📦 DESTINATAIRE - Tests des User Stories

### ✅ US-4: Consulter le Statut de Mes Colis

**Story du Brief:** "Je veux consulter le statut de colis qui me sont destinés afin de savoir quand les recevoir."

```http
GET http://localhost:8080/api/parcels/recipient/{recipient_id}
```

**Attendu:** 200 OK avec tous les colis destinés à ce destinataire, incluant leur statut actuel

---

## 🚚 LIVREUR - Tests des User Stories

### ✅ US-5: Voir Mes Colis Assignés avec Priorités et Zones

**Story du Brief:** "Je veux voir la liste de mes colis assignés, avec leurs priorités et zones."

#### Étape 1: Assigner un colis au livreur (fait par le gestionnaire)
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "deliveryPersonId": "{delivery_person_id}",
  "zoneId": "{zone_id}"
}
```

#### Étape 2: Consulter mes colis assignés
```http
GET http://localhost:8080/api/parcels/delivery-person/{delivery_person_id}
```

**Attendu:** 200 OK avec liste des colis incluant:
- Priorité (NORMAL, URGENT, EXPRESS)
- Zone assignée
- Toutes les informations du colis

---

### ✅ US-6: Mettre à Jour le Statut des Colis

**Story du Brief:** "Je veux mettre à jour le statut des colis au fur et à mesure de la collecte et de la livraison."

#### Test 6A: Collecte du Colis
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "status": "COLLECTED"
}
```
**Attendu:** 200 OK, statut = COLLECTED

#### Test 6B: Arrivée à l'Entrepôt
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "status": "IN_STOCK"
}
```
**Attendu:** 200 OK, statut = IN_STOCK

#### Test 6C: En Transit vers Livraison
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "status": "IN_TRANSIT"
}
```
**Attendu:** 200 OK, statut = IN_TRANSIT

#### Test 6D: Colis Livré
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "status": "DELIVERED"
}
```
**Attendu:** 200 OK, statut = DELIVERED

---

## 🎛️ GESTIONNAIRE LOGISTIQUE - Tests des User Stories

### ✅ US-7: Voir Demandes et Assigner aux Livreurs

**Story du Brief:** "Je veux voir toutes les demandes de livraison et les assigner aux livreurs pour planifier les tournées."

#### Test 7A: Voir Toutes les Demandes
```http
GET http://localhost:8080/api/parcels?page=0&size=20
```
**Attendu:** 200 OK avec pagination des colis

#### Test 7B: Assigner un Colis à un Livreur
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "deliveryPersonId": "{delivery_person_id}",
  "zoneId": "{zone_id}"
}
```
**Attendu:** 200 OK, colis assigné au livreur et à la zone

---

### ✅ US-8: Corriger ou Supprimer Informations Erronées

**Story du Brief:** "Je veux corriger ou supprimer des informations erronées sur un colis, client ou destinataire."

#### Test 8A: Corriger un Colis
```http
PUT http://localhost:8080/api/parcels/{parcel_id}
Content-Type: application/json

{
  "id": "{parcel_id}",
  "description": "Description corrigée",
  "weight": 0.6
}
```
**Attendu:** 200 OK avec données mises à jour

#### Test 8B: Corriger un Client Expéditeur
```http
PUT http://localhost:8080/api/sender-clients/{sender_client_id}
Content-Type: application/json

{
  "id": "{sender_client_id}",
  "phone": "+212611111122"
}
```
**Attendu:** 200 OK

#### Test 8C: Supprimer un Destinataire
```http
DELETE http://localhost:8080/api/recipients/{recipient_id}
```
**Attendu:** 204 No Content

---

### ✅ US-9: Filtrer et Paginer par Statut, Zone, Ville, Priorité, Date

**Story du Brief:** "Je veux filtrer et paginer les colis par statut, zone, ville, priorité ou date."

#### Test 9A: Filtrer par Statut
```http
GET http://localhost:8080/api/parcels/search?status=IN_TRANSIT&page=0&size=10
```

#### Test 9B: Filtrer par Zone
```http
GET http://localhost:8080/api/parcels/search?zoneId={zone_id}&page=0&size=10
```

#### Test 9C: Filtrer par Ville
```http
GET http://localhost:8080/api/parcels/search?destinationCity=Casablanca&page=0&size=10
```

#### Test 9D: Filtrer par Priorité
```http
GET http://localhost:8080/api/parcels/search?priority=URGENT&page=0&size=10
```

#### Test 9E: Pagination et Tri par Date
```http
GET http://localhost:8080/api/parcels?page=0&size=10&sort=createdAt,desc
```

**Attendu pour tous:** 200 OK avec résultats filtrés et pagination

---

### ✅ US-10: Regrouper par Zone, Statut, Priorité

**Story du Brief:** "Je veux regrouper les colis par zone, statut ou priorité pour avoir une vue synthétique."

#### Test 10A: Regroupement par Statut
```http
GET http://localhost:8080/api/parcels/group-by/status
```
**Attendu:**
```json
{
  "CREATED": 5,
  "IN_TRANSIT": 10,
  "DELIVERED": 25
}
```

#### Test 10B: Regroupement par Zone
```http
GET http://localhost:8080/api/parcels/group-by/zone
```
**Attendu:**
```json
{
  "Zone Casablanca Centre": 15,
  "Zone Rabat Nord": 8,
  "Unassigned": 3
}
```

#### Test 10C: Regroupement par Priorité
```http
GET http://localhost:8080/api/parcels/group-by/priority
```
**Attendu:**
```json
{
  "NORMAL": 20,
  "URGENT": 5,
  "EXPRESS": 3
}
```

---

### ✅ US-11: Rechercher par Mot-Clé

**Story du Brief:** "Je veux pouvoir rechercher un colis, un client ou un livreur par mot-clé (nom, numéro, ville…)."

#### Test 11A: Rechercher Clients Expéditeurs
```http
GET http://localhost:8080/api/sender-clients/search?keyword=Fatima
```

#### Test 11B: Rechercher Destinataires
```http
GET http://localhost:8080/api/recipients/search?keyword=Youssef
```

#### Test 11C: Rechercher Colis par Ville
```http
GET http://localhost:8080/api/parcels/city/Casablanca
```

**Attendu pour tous:** 200 OK avec résultats correspondant au mot-clé

---

### ✅ US-12: Calculer Poids Total et Nombre par Livreur/Zone

**Story du Brief:** "Je veux calculer le poids total et le nombre de colis par livreur et par zone pour équilibrer les tournées."

#### Test 12A: Statistiques par Livreur
```http
GET http://localhost:8080/api/delivery-persons/{delivery_person_id}/stats
```
**Attendu:**
```json
{
  "totalParcels": 15,
  "totalWeight": 45.5,
  "activeParcels": 8,
  "deliveredParcels": 7
}
```

#### Test 12B: Statistiques par Zone
```http
GET http://localhost:8080/api/zones/{zone_id}/stats
```
**Attendu:**
```json
{
  "totalParcels": 25,
  "totalWeight": 78.3,
  "inTransitParcels": 10,
  "deliveredParcels": 12
}
```

---

### ⚠️ US-13: Identifier Colis en Retard/Prioritaires

**Story du Brief:** "Je veux identifier les colis en retard ou prioritaires et recevoir une alerte par email."

#### Test 13: Colis Prioritaires Non Livrés
```http
GET http://localhost:8080/api/parcels/high-priority-pending
```

**Attendu:** 200 OK avec colis URGENT/EXPRESS non livrés
**Note:** Alerte email non implémentée (BONUS)

---

### ✅ US-14: Associer Plusieurs Produits à un Colis

**Story du Brief:** "Je veux associer plusieurs produits à un colis pour gérer les colis multi-produits."

#### Créer un 2ème Produit
```http
POST http://localhost:8080/api/products
Content-Type: application/json

{
  "name": "Étui de protection",
  "category": "Accessories",
  "weight": 0.1,
  "price": 99.99
}
```
**Sauvegarder:** `{product_id_2}`

#### Créer Colis Multi-Produits
```http
POST http://localhost:8080/api/parcels
Content-Type: application/json

{
  "senderClientId": "{sender_client_id}",
  "recipientId": "{recipient_id}",
  "description": "Commande complète: Smartphone + Accessoires",
  "weight": 0.6,
  "priority": "NORMAL",
  "destinationCity": "Casablanca",
  "destinationAddress": "456 Avenue Hassan II",
  "products": [
    {
      "productId": "{product_id}",
      "quantity": 1
    },
    {
      "productId": "{product_id_2}",
      "quantity": 2
    }
  ]
}
```

**Attendu:** 201 Created avec colis contenant 2 produits différents

---

### ✅ US-15: Consulter l'Historique Complet d'un Colis

**Story du Brief:** "Je veux consulter l'historique complet d'un colis, avec toutes les étapes et commentaires."

```http
GET http://localhost:8080/api/parcels/{parcel_id}/history
```

**Attendu:** 200 OK avec chronologie complète:
```json
[
  {
    "status": "CREATED",
    "changedAt": "2025-11-07T10:00:00",
    "comment": "Parcel created"
  },
  {
    "status": "COLLECTED",
    "changedAt": "2025-11-07T11:00:00",
    "comment": "Status updated from CREATED to COLLECTED"
  },
  {
    "status": "IN_STOCK",
    "changedAt": "2025-11-07T12:00:00",
    "comment": "Status updated from COLLECTED to IN_STOCK"
  }
]
```

---

## 📊 RÉCAPITULATIF DE CONFORMITÉ

### ✅ User Stories Complètement Implémentées: 13/15

| # | User Story | Implémentée | Testable |
|---|-----------|-------------|----------|
| US-1 | Client crée demande | ✅ | ✅ |
| US-2 | Client consulte colis | ✅ | ✅ |
| US-3 | Client reçoit email | ❌ BONUS | ❌ |
| US-4 | Destinataire consulte | ✅ | ✅ |
| US-5 | Livreur voit assignés | ✅ | ✅ |
| US-6 | Livreur met à jour statut | ✅ | ✅ |
| US-7 | Gestionnaire assigne | ✅ | ✅ |
| US-8 | Gestionnaire corrige/supprime | ✅ | ✅ |
| US-9 | Gestionnaire filtre/pagine | ✅ | ✅ |
| US-10 | Gestionnaire regroupe | ✅ | ✅ |
| US-11 | Gestionnaire recherche | ✅ | ✅ |
| US-12 | Gestionnaire calcule poids | ✅ | ✅ |
| US-13 | Gestionnaire alerte prioritaires | ⚠️ Partiel | ⚠️ |
| US-14 | Gestionnaire multi-produits | ✅ | ✅ |
| US-15 | Gestionnaire historique | ✅ | ✅ |

### 📈 Score de Conformité: **86.7%** (13/15 complètes)

**Fonctionnalités BONUS non implémentées:**
- US-3: Notification email client expéditeur
- US-13: Alerte email colis prioritaires (identification OUI, email NON)

---

## 🎯 ORDRE DE TEST RECOMMANDÉ

1. **Prérequis** (5 étapes) - Créer les données de base
2. **US-1** - Client crée demande
3. **US-14** - Gestionnaire multi-produits (créer 2ème colis)
4. **US-7** - Gestionnaire assigne aux livreurs
5. **US-5** - Livreur voit assignés
6. **US-6** - Livreur met à jour statuts (4 transitions)
7. **US-2** - Client consulte en cours/livrés
8. **US-4** - Destinataire consulte
9. **US-15** - Gestionnaire historique
10. **US-9** - Gestionnaire filtre/pagine (5 tests)
11. **US-10** - Gestionnaire regroupe (3 tests)
12. **US-11** - Gestionnaire recherche (3 tests)
13. **US-12** - Gestionnaire calcule poids (2 tests)
14. **US-13** - Gestionnaire prioritaires
15. **US-8** - Gestionnaire corrige/supprime (3 tests)

**Total:** 5 prérequis + 32 tests couvrant 15 user stories

---

## ⚠️ IMPORTANT

**Ce guide contient UNIQUEMENT les fonctionnalités du brief.**

**Endpoints NON testés ici (car hors brief):**
- Compter colis assignés
- Statistiques détaillées livreur
- Colis urgents spécifiques
- CRUD complet sur zones/produits
- Livreurs disponibles
- Authentification/Autorisation

**Ces fonctionnalités sont utiles mais ne font pas partie des 15 user stories à livrer.**

---

*Guide généré pour SmartLogi V0.1.0 - Novembre 2025*
