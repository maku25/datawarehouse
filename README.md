# Projet BI : Aide à la décision pour investisseur (Yelp)

## 1. Vision et objectifs
L'objectif de ce projet est de transformer la donnée brute issue de l'écosystème Yelp en un **outil décisionnel** pour un investisseur. 

En l'absence de données financières privées (CA, marges), nous avons développé une approche basée sur des **indicateurs de substitution (proxys)** pour évaluer la valeur d'un commerce :
* **Qualité de l'Engagement :** Analyse de la précision et de la pertinence des avis (longueur, votes).
* **Dynamique de Flux :** Mesure de la popularité via les check-ins.
* **Potentiel de Zone :** Analyse de la saturation géographique pour identifier les opportunités d'implantation.

---

## 2. Architecture Technique
Le pipeline de données est structuré pour garantir performance et scalabilité :

1. **Sources hétérogènes :** - Base PostgreSQL (Avis, Utilisateurs).
   - Fichiers JSON (Commerces, Check-ins).
2. **Traitement ETL :** - Moteur **Apache Spark (Scala)** sur le serveur `stendhal`.
3. **Stockage Décisionnel :** - Entrepôt **Oracle** sur le serveur `enss2025`.



---

## 3. Modélisation : Schéma en Constellation
Nous avons implémenté un **Schéma en Constellation (Galaxy Schema)**. Ce choix est justifié par la nécessité de piloter trois processus métiers distincts tout en partageant des dimensions communes (conformées) (à creuser encore plus après):

* **FAIT_PERFORMANCE :** Évaluation de la satisfaction et de l'impact social.
* **FAIT_MARCHE_LOCAL :** Analyse macro-économique des quartiers.
* **FAIT_FIDELITE_CLIENT :** Analyse de la rétention et de la typologie des consommateurs.

**Dimensions pivots :** Temps, Géographie, Commerce, Utilisateur.

