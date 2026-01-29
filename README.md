Projet innformatique décisionnelle

étape 1 :
obj métier = identifier les opportunités d'implantation et les pépites à acheter

(on a pas les chiffres CA, benefs etc..)

étape 2 : 
approche kimball avec schéma en étoile

table de faits : FAIT_INVESTISSEMENT (mesures: notes, checkins, sentiment, votes)
4 dims : DIM_BUSINESS (service, prix), DIM_GEO (quartiers, loc gps), DIM_TEMPS (saisons, années), DIM_UTILISATEUR (élites, influenceurs) 

script sql fait via sql developper



- code spark (scala)


-----
## Obj du projet : 
Ce projet vise à fournir un outil d'aide à la décision pour un investisseur dans le secteur des services (restaurants, bars, commerces). 

L'objectif est de transformer les données brutes de Yelp pour répondre à trois problématiques majeures :
- Performance : quels sont les établissements qui satisfont réellement leurs clients ?

- Opportunité de marché : Quels quartiers sont sous-équipés ou en forte demande ?

- Fidélisation : Quelle est la solidité de la base client d'un commerce ?


## Architecture de la solution : 
Sources de données : Données hétérogènes (PostgreSQL pour les avis/utilisateurs, fichiers JSON pour les commerces/check-ins).

Moteur etl (serv stendhal) : Utilisation de Spark (Scala) pour le nettoyage, la jointure et l'agrégation des données.

Entrepôt de données (serv enss2025) : Base de données Oracle structurée pour le décisionnel (Schéma en Constellation).

## Strat de modélisation
### Schéma en constellation
COntrairement à un schéma en étoile classique, nnous avons opté pour une consotellation de faits.
Ce choix stratégique permet de partager des dimensions conformées entre plusieurs processus métiers.

**Avantages :**
* Cohérence totale : une dim DIM_GEOGRAPHIE identique pour tous les rapports
* Analyse croisée : Possibilité de corréler la note d'un établissement (`FAIT_PERFORMANCE`) avec l'attractivité de sa zone géographique (`FAIT_MARCHE`).

## Matrice de bus (méthodologie Kimball)
La matrice ci-dessous définit la granularité et les axes d'analyse de notre entrepôt de données.

| Dimensions / Faits | FAIT_PERFORMANCE | FAIT_MARCHE_LOCAL | FAIT_FIDELITE_CLIENT |
| :--- | :---: | :---: | :---: |
| **DIM_TEMPS** | ✅ | ✅ | ✅ |
| **DIM_GEOGRAPHIE** | ✅ | ✅ | |
| **DIM_COMMERCE** | ✅ | ✅ | |
| **DIM_UTILISATEUR** | ✅ | | |
| **DIM_PROFIL_CLIENT** | | | ✅ |