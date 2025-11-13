# Plan de Travail Détaillé - Tests et Qualité v0.1.1

**Projet:** SmartLogi Delivery Management System
**Version:** 0.1.1
**Période:** 10/11/2025 - 14/11/2025 (4 jours)
**Assigné:** Hajar Walfi
**Objectif:** Atteindre >90% de couverture de tests et garantir la qualité du code

---

## 📊 Vue d'ensemble

| Métrique | Valeur |
|----------|--------|
| **Total Story Points** | 65 |
| **Nombre d'Epics** | 1 |
| **Nombre de Stories** | 7 |
| **Nombre de Tasks** | 46 |
| **Durée** | 4 jours |

---

## 🎯 Epic Principal

### **Epic-1: Tests et Qualité - SmartLogi v0.1.1**
- **Story Points:** -
- **Durée:** 10/11/2025 → 14/11/2025
- **Description:** Phase de tests et assurance qualité pour atteindre 90% de couverture

---

## 📋 Stories et Tasks Détaillées

### **JOUR 1 - Lundi 10/11/2025**

#### **Story-1: Configuration des outils de test et environnement** ⚙️
**Story Points:** 5 | **Priorité:** High

| Task | Description | Points | Statut |
|------|-------------|--------|--------|
| Task-1.1 | Mettre à jour pom.xml vers version 0.1.1 | 1 | ✅ FAIT |
| Task-1.2 | Configurer JaCoCo Maven Plugin | 2 | ✅ FAIT |
| Task-1.3 | Ajouter dépendance H2 Database | 1 | ✅ FAIT |
| Task-1.4 | Créer application.yml pour tests | 2 | ✅ FAIT |
| Task-1.5 | Créer structure de répertoires de test | 1 | ✅ FAIT |

#### **Story-2: Tests unitaires - Services Layer (Début)** 🧪
**Story Points:** 21 | **Priorité:** Highest

| Task | Description | Tests Min | Points | Statut |
|------|-------------|-----------|--------|--------|
| Task-2.1 | Tests ProductService | 15+ tests | 3 | ✅ FAIT |
| Task-2.2 | Tests ZoneService | 20+ tests | 3 | ✅ FAIT |
| Task-2.3 | Tests RecipientService | 12+ tests | 2 | ✅ FAIT |

---

### **JOUR 2 - Mardi 11/11/2025**

#### **Story-2: Tests unitaires - Services Layer (Suite)** 🧪

| Task | Description | Tests Min | Points | Deadline |
|------|-------------|-----------|--------|----------|
| Task-2.4 | Tests SenderClientService | 12+ tests | 3 | 11/11 |
| Task-2.5 | Tests DeliveryPersonService | 15+ tests | 3 | 12/11 |
| Task-2.6 | Tests ParcelService | 18+ tests | 4 | 12/11 |

**Focus Jour 2:**
- Finaliser tests SenderClientService (CRUD + validation email unique)
- Commencer tests DeliveryPersonService (affectations zones, statistiques)
- Commencer tests ParcelService (gestion statuts, filtres complexes)

---

### **JOUR 3 - Mercredi 12/11/2025**

#### **Story-2: Tests unitaires - Services Layer (Fin)** 🧪

| Task | Description | Tests Min | Points | Deadline |
|------|-------------|-----------|--------|----------|
| Task-2.7 | Tests ParcelProductService | 10+ tests | 2 | 12/11 |
| Task-2.8 | Tests DeliveryHistoryService | 8+ tests | 2 | 12/11 |
| Task-2.9 | Tests StatisticsService | 10+ tests | 3 | 12/11 |

#### **Story-3: Tests unitaires - Controllers Layer (Début)** 🎮
**Story Points:** 13 | **Priorité:** High

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-3.1 | Tests ProductController (MockMvc) | 2 | 12/11 |
| Task-3.2 | Tests ZoneController (MockMvc) | 2 | 12/11 |
| Task-3.3 | Tests RecipientController (MockMvc) | 1 | 13/11 |
| Task-3.4 | Tests SenderClientController (MockMvc) | 1 | 13/11 |

**Focus Jour 3:**
- Finaliser tous les tests de services
- Commencer tests controllers avec MockMvc
- Valider endpoints REST, codes HTTP, validation DTOs

---

### **JOUR 4 - Jeudi 13/11/2025**

#### **Story-3: Tests unitaires - Controllers Layer (Suite)** 🎮

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-3.5 | Tests DeliveryPersonController (MockMvc) | 2 | 13/11 |
| Task-3.6 | Tests ParcelController (MockMvc) | 2 | 13/11 |
| Task-3.7 | Tests ParcelProductController (MockMvc) | 1 | 13/11 |
| Task-3.8 | Tests DeliveryHistoryController (MockMvc) | 1 | 13/11 |
| Task-3.9 | Tests StatisticsController (MockMvc) | 1 | 13/11 |

#### **Story-4: Tests d'intégration** 🔗
**Story Points:** 8 | **Priorité:** High

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-4.1 | Tests d'intégration REST endpoints complets | 3 | 13/11 |
| Task-4.2 | Tests d'intégration Repository layer | 2 | 13/11 |
| Task-4.3 | Tests d'intégration transactions | 2 | 13/11 |
| Task-4.4 | Tests d'intégration Liquibase | 1 | 13/11 |

#### **Story-5: Analyse qualité et couverture (Début)** 📊
**Story Points:** 8 | **Priorité:** High

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-5.1 | Exécuter tests et générer rapport JaCoCo | 1 | 13/11 |
| Task-5.2 | Analyser le rapport JaCoCo | 2 | 13/11 |
| Task-5.3 | Ajouter tests manquants pour atteindre 90% | 3 | 14/11 |
| Task-5.4 | Configurer SonarQube localement | 1 | 13/11 |
| Task-5.5 | Ajouter plugin SonarQube dans pom.xml | 1 | 13/11 |

**Focus Jour 4:**
- Finaliser tous les tests unitaires et d'intégration
- Première analyse de couverture
- Configuration SonarQube

---

### **JOUR 5 - Vendredi 14/11/2025** (DEADLINE)

#### **Story-5: Analyse qualité et couverture (Fin)** 📊

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-5.6 | Exécuter analyse SonarQube | 1 | 14/11 |
| Task-5.7 | Analyser résultats SonarQube | 2 | 14/11 |
| Task-5.8 | Corriger anomalies critiques SonarQube | 3 | 14/11 |

#### **Story-6: Documentation et rapports finaux** 📝
**Story Points:** 5 | **Priorité:** High

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-6.1 | Mettre à jour README.md - Section Tests | 2 | 14/11 |
| Task-6.2 | Documenter les résultats de couverture | 1 | 14/11 |
| Task-6.3 | Documenter les résultats SonarQube | 1 | 14/11 |
| Task-6.4 | Créer section Actions d'amélioration | 1 | 14/11 |
| Task-6.5 | Créer TestGuide.md | 1 | 14/11 |
| Task-6.6 | Export rapports JaCoCo et SonarQube | 1 | 14/11 |

#### **Story-7: Préparation présentation** 🎤
**Story Points:** 5 | **Priorité:** High

| Task | Description | Points | Deadline |
|------|-------------|--------|----------|
| Task-7.1 | Préparer démo fonctionnalités (10 min) | 2 | 14/11 |
| Task-7.2 | Préparer explication architecture et tests (10 min) | 2 | 14/11 |
| Task-7.3 | Préparer mise en situation (5 min) | 1 | 14/11 |
| Task-7.4 | Préparer Q&A (5 min) | 1 | 14/11 |

**Focus Jour 5:**
- Finaliser analyse SonarQube et corrections
- Compléter toute la documentation
- Préparer présentation de 30 minutes

---

## 📈 Répartition des Story Points par Jour

| Jour | Date | Story Points | Tâches |
|------|------|--------------|--------|
| **J1** | 10/11 | 5 + 8 = **13 SP** | Configuration + Début Services |
| **J2** | 11/11 | 10 **SP** | Suite Services |
| **J3** | 12/11 | 9 + 4 = **13 SP** | Fin Services + Début Controllers |
| **J4** | 13/11 | 9 + 8 + 5 = **22 SP** | Fin Controllers + Intégration + Analyse |
| **J5** | 14/11 | 3 + 5 + 5 = **13 SP** | SonarQube + Documentation + Présentation |

**Total: 65 Story Points sur 4 jours**

---

## 🎯 Critères de Succès

### Livrables Obligatoires
- ✅ Code source avec classes de test
- ✅ Rapport JaCoCo avec >90% de couverture
- ✅ Rapport SonarQube complet
- ✅ README.md mis à jour
- ✅ TestGuide.md créé

### Métriques de Qualité
- **Couverture de code:** >90%
- **Quality Gate SonarQube:** PASSED
- **Bugs critiques:** 0
- **Vulnérabilités:** 0
- **Code Smells:** <50
- **Duplication:** <5%

### Présentation (30 minutes)
1. **Démo fonctionnalités** - 10 min
2. **Explication code et architecture** - 10 min
3. **Mise en situation** - 5 min
4. **Q&A** - 5 min

---

## 🛠️ Outils et Technologies

| Outil | Version | Usage |
|-------|---------|-------|
| **JUnit 5** | 5.10.x | Framework de tests unitaires |
| **Mockito** | 5.x | Mocking des dépendances |
| **Spring Boot Test** | 3.3.5 | Tests d'intégration |
| **MockMvc** | - | Tests controllers REST |
| **H2 Database** | 2.x | Base de données en mémoire |
| **JaCoCo** | 0.8.11 | Couverture de code |
| **SonarQube** | Latest | Analyse qualité du code |
| **AssertJ** | 3.x | Assertions fluides |

---

## 📝 Conventions de Tests

### Nommage des Tests
```java
@Test
@DisplayName("Should [action] when [condition]")
void test[MethodName]_[Scenario]() {
    // Given - Arrange
    // When - Act
    // Then - Assert
}
```

### Structure des Packages
```
src/test/java/com/logismart/logismartv2/
├── service/           # Tests unitaires services
├── controller/        # Tests unitaires controllers
├── integration/       # Tests d'intégration
└── repository/        # Tests repositories (optionnel)
```

### Annotations Principales
- `@ExtendWith(MockitoExtension.class)` - Tests unitaires
- `@SpringBootTest` - Tests d'intégration
- `@WebMvcTest` - Tests controllers isolés
- `@DataJpaTest` - Tests repositories
- `@Mock` - Mock des dépendances
- `@InjectMocks` - Injection des mocks

---

## 📌 Import dans Jira

### Méthode 1: Import CSV
1. Aller dans Jira → **Projects** → Votre projet
2. Cliquer sur **"..."** → **Import issues from CSV**
3. Sélectionner le fichier `jira-import-plan.csv`
4. Mapper les colonnes selon le template Jira
5. Importer

### Méthode 2: Création Manuelle via Excel
1. Ouvrir le fichier CSV dans Excel
2. Créer les issues une par une en copiant les informations
3. Utiliser la hiérarchie: Epic → Story → Task

---

## 🔄 Suivi et Mise à Jour

### Daily Progress
Mettre à jour quotidiennement:
- ✅ Tasks complétées
- 🔄 Tasks en cours
- ⏳ Tasks à venir
- 🚨 Blocages identifiés

### Jalons Critiques
- **10/11 EOD:** Configuration terminée
- **12/11 EOD:** Tous tests services terminés
- **13/11 EOD:** Tests controllers et intégration terminés + JaCoCo >90%
- **14/11 EOD:** SonarQube OK + Documentation complète + Présentation prête

---

## ⚠️ Risques et Mitigation

| Risque | Probabilité | Impact | Mitigation |
|--------|-------------|--------|------------|
| Couverture <90% | Moyenne | Haut | Buffer de temps J4 pour tests additionnels |
| Problèmes SonarQube | Faible | Moyen | Configuration anticipée J4 |
| Tests complexes longs | Haute | Moyen | Focus sur tests critiques d'abord |
| Retards documentation | Moyenne | Faible | Templates préparés à l'avance |

---

**Créé le:** 11/11/2025
**Par:** Hajar Walfi
**Status:** 🟢 EN COURS
