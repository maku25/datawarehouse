On a récupéré toutes les données et analyser le schéma de chaque fichier et table de la bd postgresql (data/).

## Pb globale : 
>  Comment identifier les segments de marché (Ville x Secteur) présentant le plus fort potentiel de croissance et définir la stratégie opérationnelle pour y réussir ?

### Obj 1 : Identification des opportunités
Le but n'est pas seulement de regarder une ville, mais de comparer les déséquilibres entre l'Offre (nombre d'établissements) et la Demande (volume d'avis).

* **Livrable :** Un TOP 15 des couples Ville/Secteur.
* **Indicateur Clé (KPI) :** La Densité de clients par établissement (Total Avis / Nombre de Commerces).
* **Le "Plus" stratégique :** Identifier les zones à forte densité mais à Note Moyenne faible (là où les clients sont nombreux mais insatisfaits par l'offre actuelle).

### Obj 2 : La "Recette du Succès" (Optimisation du Produit)
Une fois le secteur identifié, il faut définir les caractéristiques gagnantes du futur établissement.

* **Livrable :** Analyse de corrélation entre les services (Parking, WiFi, Accessibilité, Terrasse) et la Note Moyenne.
* **Focus Technique :** Valorisation du nettoyage de données (Data Engineering). Prouver que l'intégration des données de Parking (initialement manquantes/complexes) modifie la compréhension du succès.
* **Décision :** Déterminer la liste des équipements "Must-Have" pour surpasser la concurrence locale.- comprendre quels services spécifiques boostent la satisfaction client et la réputation.

### Objectif 3 : Segmentation et Influence (Ciblage Client)
Identifier qui sont les prescripteurs de réputation pour garantir un lancement réussi.

* **Livrable :** Profilage des membres Elite.
* **Analyse :** Comparer l'impact d'un avis "Elite" par rapport à un avis standard sur la note globale.
* **Actionable :** Définir une stratégie marketing ciblée sur les 5% d'utilisateurs qui génèrent 80% de l'influence numérique.

### Objectif 4 : Maîtrise du "Time-to-Market" (Saisonnalité)
Optimiser la date d'ouverture pour maximiser le chiffre d'affaires et la bienveillance des clients.

* **Livrable :** Courbe de saisonnalité croisant Volume d'activité et Satisfaction.
* **Décision :** Choisir le mois d'inauguration idéal (ex: Juin pour roder l'équipe avant le pic de Juillet).

---
> *"Le projet a évolué d'une analyse descriptive locale (Henderson) vers une approche comparative multi-secteurs. L'innovation réside dans la création d'un Indicateur de Tension de Marché permettant de prioriser les investissements là où la demande est forte et l'offre qualitative défaillante."*
---
  
modélisation schéma : approche kimball en constellation (dim_fait.md) OK

bd oracle (oracle.md) (sqlplus login/login@enss2025) OK

etl spark OK

analyse 

restitution metabase

rapport
