# 📐 Modélisation Dimensionnelle de l'Entrepôt de Données (Oracle)

Ce document formalise la conception et l'implémentation physique du schéma de l'entrepôt de données Yelp hébergé sur **Oracle SQL**. L'architecture adopte une modélisation en **Schéma en Étoile (Star Schema)** optimisée pour les requêtes analytiques décisionnelles (OLAP).

---

## 🏗️ Architecture Globale du Schéma

Le modèle est articulé autour de **3 tables de faits** centrales modélisant l'activité observable (les interactions), reliées à **5 tables de dimensions** (les axes d'analyse).

Afin de gérer la relation de cardinalité de type *Many-to-Many* (Plusieurs-à-Plusieurs) entre un commerce et ses multiples catégories d'activité sans dupliquer les lignes de faits, une **Table de Pont (Bridge Table)** a été mise en œuvre.

---

## 🗄️ Dictionnaire des Tables & Scripts DDL

### 1. Tables de Dimensions (Axes Analytiques)

#### 🗺️ `DIM_GEOGRAPHIE`

Centralise les données de localisation physique et géospatiale pour l'analyse cartographique (Heatmaps).

```sql
CREATE TABLE DIM_GEOGRAPHIE (
    GEO_ID       NUMBER PRIMARY KEY,
    ADRESSE      VARCHAR2(4000),
    VILLE        VARCHAR2(255),
    ETAT         VARCHAR2(50),
    CODE_POSTAL  VARCHAR2(20),
    LATITUDE     NUMBER,
    LONGITUDE    NUMBER
);
```

#### 📅 `DIM_TEMPS`

Dimension de temps granulaire permettant les analyses de saisonnalité (Heures/Jours/Mois) et l'isolation des comportements en week-end.

```sql
CREATE TABLE DIM_TEMPS (
    DATE_ID        NUMBER PRIMARY KEY,
    DATE_COMPLETE  DATE NOT NULL,
    ANNEE          NUMBER(4) NOT NULL,
    TRIMESTRE      NUMBER(1) NOT NULL,
    MOIS           NUMBER(2) NOT NULL,
    NOM_MOIS       VARCHAR2(20),
    JOUR           NUMBER(2) NOT NULL,
    JOUR_SEMAINE   VARCHAR2(20),
    EST_WEEKEND    NUMBER(1)
);
```

#### 👥 `DIM_UTILISATEUR`

Stocke les attributs et profils de consommation des utilisateurs (identification des segments d'influence « Elite »).

```sql
CREATE TABLE DIM_UTILISATEUR (
    USER_ID          VARCHAR2(22) PRIMARY KEY,
    NOM              VARCHAR2(255),
    DATE_INSCRIPTION DATE,
    NB_AVIS_TOTAL    NUMBER(10),
    NB_FANS          NUMBER(10),
    NOTE_MOY_DONNEE  NUMBER(3,2),
    EST_ELITE        NUMBER(1),
    NB_ANNEES_ELITE  NUMBER(5) DEFAULT 0
);
```

#### 🍽️ `DIM_COMMERCE`

Contient l'ensemble des caractéristiques opérationnelles et attributs de service des commerces et restaurants.

```sql
CREATE TABLE DIM_COMMERCE (
    COMMERCE_ID         VARCHAR2(22) PRIMARY KEY,
    GEO_ID              NUMBER REFERENCES DIM_GEOGRAPHIE(GEO_ID),
    NOM                 VARCHAR2(255) NOT NULL,
    CATEGORIES          VARCHAR2(1000),
    EST_OUVERT          NUMBER(1),
    NOTE_MOYENNE_SOURCE NUMBER(3,2),
    NB_AVIS_SOURCE      NUMBER(10),
    GAMME_PRIX          NUMBER(1),
    WIFI                VARCHAR2(20),
    ALCOOL              VARCHAR2(50),
    PARKING_GARAGE      NUMBER(1),
    PARKING_RUE         NUMBER(1),
    ACCEPTE_CB          NUMBER(1),
    TERRASSE            NUMBER(1),
    MENU_VEGE           NUMBER(1),
    ADAPTE_ENFANTS      NUMBER(1),
    LIVRAISON           NUMBER(1),
    VENTE_A_EMPORTER    NUMBER(1),
    ACCES_HANDICAPE     NUMBER(1)
);
```

---

### 2. Modélisation Multi-Catégories : Table de Pont (Bridge Table)

Un établissement pouvant appartenir à plusieurs catégories sémantiques (ex : *Japanese*, *Sushi Bars*, *Ramen*), l'utilisation d'une simple clé étrangère classique provoquerait une explosion combinatoire des lignes. La structure ci-dessous isole les dimensions catégorielles.

#### 🏷️ `DIM_CATEGORIE` & `PONT_COMMERCE_CATEGORIE`

```sql
CREATE TABLE DIM_CATEGORIE (
    CATEGORIE_ID    NUMBER PRIMARY KEY,
    NOM_CATEGORIE   VARCHAR2(255) NOT NULL,
    CONSTRAINT UK_CATEGORIE_NOM UNIQUE (NOM_CATEGORIE)
);

CREATE TABLE PONT_COMMERCE_CATEGORIE (
    COMMERCE_ID   VARCHAR2(22) NOT NULL REFERENCES DIM_COMMERCE(COMMERCE_ID),
    CATEGORIE_ID  NUMBER NOT NULL REFERENCES DIM_CATEGORIE(CATEGORIE_ID),
    CONSTRAINT PK_PONT_CAT PRIMARY KEY (COMMERCE_ID, CATEGORIE_ID)
);
```

---

### 3. Tables de Faits (Indicateurs & Métriques Quantitatives)

#### 📝 `FAIT_REVIEWS`

Enregistre les transactions d'évaluation, les notations et l'impact d'engagement (votes de la communauté).

```sql
CREATE TABLE FAIT_REVIEWS (
    REVIEW_ID    VARCHAR2(22) PRIMARY KEY,
    COMMERCE_ID  VARCHAR2(22) NOT NULL REFERENCES DIM_COMMERCE(COMMERCE_ID),
    USER_ID      VARCHAR2(22) NOT NULL REFERENCES DIM_UTILISATEUR(USER_ID),
    DATE_ID      NUMBER NOT NULL REFERENCES DIM_TEMPS(DATE_ID),
    NOTE         NUMBER(1),
    VOTES_UTILE  NUMBER DEFAULT 0,
    VOTES_DROLE  NUMBER DEFAULT 0,
    VOTES_COOL   NUMBER DEFAULT 0
);
```

#### 📈 `FAIT_FLUX`

Mesure la fréquentation des établissements à travers les données d'enregistrement de présence (check-ins).

```sql
CREATE TABLE FAIT_FLUX (
    COMMERCE_ID  VARCHAR2(22) NOT NULL REFERENCES DIM_COMMERCE(COMMERCE_ID),
    DATE_ID      NUMBER NOT NULL REFERENCES DIM_TEMPS(DATE_ID),
    NB_CHECKINS  NUMBER DEFAULT 0,
    CONSTRAINT PK_FAIT_FLUX PRIMARY KEY (COMMERCE_ID, DATE_ID)
);
```

#### 💡 `FAIT_TIPS`

Quantifie les retours d'expérience rapides et astuces laissés par les consommateurs.

```sql
CREATE TABLE FAIT_TIPS (
    COMMERCE_ID     VARCHAR2(22) NOT NULL REFERENCES DIM_COMMERCE(COMMERCE_ID),
    USER_ID         VARCHAR2(22) NOT NULL REFERENCES DIM_UTILISATEUR(USER_ID),
    DATE_ID         NUMBER NOT NULL REFERENCES DIM_TEMPS(DATE_ID),
    NB_COMPLIMENTS  NUMBER DEFAULT 0
);
```

---

## 📊 Synthèse du Modèle

| Type de table | Nom | Rôle |
|---|---|---|
| Dimension | `DIM_GEOGRAPHIE` | Localisation et géospatial |
| Dimension | `DIM_TEMPS` | Saisonnalité et chronologie |
| Dimension | `DIM_UTILISATEUR` | Profils consommateurs |
| Dimension | `DIM_COMMERCE` | Caractéristiques des établissements |
| Dimension | `DIM_CATEGORIE` | Référentiel des catégories |
| Pont | `PONT_COMMERCE_CATEGORIE` | Relation N-N commerce ↔ catégorie |
| Fait | `FAIT_REVIEWS` | Avis et notations |
| Fait | `FAIT_FLUX` | Check-ins / fréquentation |
| Fait | `FAIT_TIPS` | Tips et compliments |