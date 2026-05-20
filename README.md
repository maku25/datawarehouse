# Pipeline ETL hétérogène & datawarehouse Yelp (Oracle)

[![Language: Scala](https://img.shields.io/badge/Language-Scala%2094%25-red?style=flat-square)](https://www.scala-lang.org/)
[![Language: Python](https://img.shields.io/badge/Language-Python%206%25-blue?style=flat-square)](https://www.python.org/)
[![Database: Oracle](https://img.shields.io/badge/Data%20Warehouse-Oracle%20SQL-red?style=flat-square)](https://www.oracle.com/database/)
[![Database: PostgreSQL](https://img.shields.io/badge/Source%20DB-PostgreSQL-blue?style=flat-square)](https://www.postgresql.org/)

## 📝 Description du Projet
Ce projet de business intelligence implémente un pipeline ETL multi-sources et un datawarehouse complet basé sur le jeu de données hétérogène de **Yelp**. 

La problématique business centrale est la suivante : **Comment identifier les marchés géographiques présentant le plus fort potentiel pour l'implantation d'un nouveau restaurant, et définir la stratégie opérationnelle optimale pour garantir sa réussite ?**

## 🏗️ Architecture Data & Flux ETL
Le projet met en œuvre une architecture d'intégration de données hétérogènes :
1. **Sources de données brutes (Multi-Sources) :** - Une partie des données Yelp pré-stockée dans une base relationnelle **PostgreSQL**.
   - Le reste des données brutes stocké sous forme de fichiers plats semi-structurés (**JSON** et **CSV**).
2. **Pipeline ETL (Scala) :** Extraction simultanée depuis la base PostgreSQL et les fichiers plats, nettoyage, réconciliation des schémas, typage et transformation.
3. **Entrepôt Cible (Oracle) :** Chargement des données nettoyées dans un Data Warehouse **Oracle SQL** modélisé en schéma en étoile (tables de faits et dimensions optimisées pour le décisionnel / OLAP).

---

## 🎯 Objectifs Décisionnels & Analytiques (Requêtes OLAP)
Une fois centralisées dans Oracle, les données permettent de répondre à 4 axes stratégiques :
1. 🗺️ **Analyse Géographique :** Identification des zones à forte densité de clients mais à faible concurrence de qualité.
2. 🍽️ **Optimisation de l'Offre :** Corrélation entre les attributs des restaurants (services, menus) et les notes obtenues.
3. 👥 **Segmentation Client & Influence :** Analyse du comportement des utilisateurs et de l'impact des "Elite Users" sur la réputation.
4. 📉 **Analyse Temporelle & Saisonnalité :** Modélisation des tendances de consommation selon les saisons et les années.

---

## 📂 Structure du Dépôt

```text
├── data/
│   ├── bd_postgresql/         # Documentation et schémas de la base PostgreSQL source (Données brutes)
│   └── donnees_yelp/          # Spécifications des fichiers JSON/CSV sources et script Python de parsing (business.py)
├── script_etl/                # Pipeline ETL principal (Extraction multi-sources -> Transformation -> Chargement Oracle) en Scala
├── bd.md                      # Documentation technique du modèle conceptuel et du schéma en étoile cible sur Oracle (Faits & Dimensions)
├── requetes_sql_dashboard.sql # Catalogue de requêtes analytiques OLAP optimisées pour Oracle SQL
└── .gitignore                 # Exclusion des volumes de données brutes du versioning

---

## 🛠️ Stack Technique

- **Langages :** Scala (Cœur du pipeline ETL / manipulation des flux), Python (Parsing et preprocessing des fichiers JSON).
- **Infrastructures Data :** PostgreSQL (Source relationnelle), Oracle SQL (Entrepôt décisionnel final).
- **Business Intelligence :** Préparation des vues d'agrégation prêtes pour des outils de datavisualisation (Metabase, Power BI, Superset).

---

## 🚀 Utilisation & Exécution

### 1. Analyse des sources de fichiers plats
Le script Python permet d'analyser la structure et de valider l'intégrité des objets JSON complexes fournis par Yelp avant extraction :
python data/donnees_yelp/business.py

### 2. Orchestration du Pipeline ETL
Les traitements lourds de nettoyage, de jointure multi-sources et d'alimentation du schéma en étoile (détaillé dans bd.md) sont exécutés par les scripts Scala du dossier /script_etl.

### 3. Analyses Décisionnelles (Oracle)
Les indicateurs de performance clés (KPIs) répondant aux 4 objectifs business s'obtiennent en exécutant les requêtes OLAP optimisées sur la base Oracle :
# Requêtes disponibles dans requetes_sql_dashboard.sql

