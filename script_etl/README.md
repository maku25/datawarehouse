# ⚙️ Pipeline d'Orchestration ETL (Scala / Apache Spark)

Ce répertoire regroupe les scripts d'extraction, de transformation et de chargement (ETL) développés en **Scala** à l'aide du framework distribué **Apache Spark**. 

Le pipeline extrait des données brutes stockées sur des sources hétérogènes (fichiers semi-structurés JSON/CSV locaux et base relationnelle PostgreSQL de production) afin de nettoyer, consolider et alimenter l'entrepôt de données modélisé en étoile sur **Oracle SQL**.

---

## 🏗️ Architecture et Découpage Modulaire

Conformément aux principes du génie logiciel de responsabilité unique (*Single Responsibility Principle*), le pipeline est segmenté en fichiers indépendants ordonnés selon les contraintes d'intégrité de clé étrangère :

```text
├── dim_temps.scala       # Étape 1 : Génération du calendrier continu dimensionnel
├── dim_geographie.scala  # Étape 2 : Construction du dictionnaire unique des adresses
├── dim_commerce.scala    # Étape 3 : Jointure géo, aplanissement JSON des options/attributs
├── dim.categorie.scala   # Étape 4 : Éclatement (Explode) et génération de la table de pont (N:N)
├── dim_utilisateur.scala # Étape 5 : Agrégation historique (PostgreSQL) et classification Élite
├── dim_reviews.scala     # Étape 6 : Alimentation des faits d'évaluation (Double jointure de sécurité)
├── dim_flux.scala        # Étape 7 : Extraction et agrégation des enregistrements de visite (Check-ins)
└── fait_tips.scala       # Étape 8 : Calcul des indicateurs d'avis courts (Tips & Compliments)