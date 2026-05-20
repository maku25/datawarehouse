# Spécifications des fichiers plats (json / csv)

Cette section documente les structures des données brutes de Yelp utilisées comme sources secondaires pour l'entrepôt décisionnel. Ces formats semi-structurés font l'objet d'un aplanissement (flattening) et d'un nettoyage via le script de preprocessing `business.py` avant leur intégration.

## 1. Fichier `business.json`

Ce jeu de données présente un fort niveau d'imbrication (dictionnaires imbriqués et types complexes) pour modéliser les attributs et les horaires des établissements.

### Données de premier niveau
- `business_id` *(str)* : Identifiant unique de l'établissement
- `name` *(str)* : Nom du commerce
- `city` *(str)*, `state` *(str)*, `postal_code` *(str)* : Localisation géographique
- `latitude` *(float)*, `longitude` *(float)* : Coordonnées géospatiales
- `stars` *(float)* : Note moyenne globale
- `review_count` *(int)* : Nombre total d'avis
- `is_open` *(int)* : Statut d'activité (0 pour fermé, 1 pour ouvert)
- `categories` *(str)* : Liste des mots-clés sectoriels associés

### Objets complexes imbriqués (Dictionnaires)
- `hours` *(dict)* : Horaires d'ouverture quotidiens (`Monday`, `Tuesday`, etc.)
- `attributes` *(dict)* : Caractéristiques de service complexes, contenant des sous-structures requérant un traitement récursif :
  - **Ambience** *(dict)* : `casual`, `classy`, `divey`, `hipster`, `intimate`, `romantic`, `touristy`, `trendy`, `upscale` *(bool)*
  - **BusinessParking** *(dict)* : `garage`, `lot`, `street`, `valet`, `validated` *(bool)*
  - **GoodForMeal** *(dict)* : `breakfast`, `brunch`, `dessert`, `dinner`, `latenight`, `lunch` *(bool)*
  - **DietaryRestrictions** *(dict)* : `dairy-free`, `gluten-free`, `halal`, `kosher`, `soy-free`, `vegan`, `vegetarian` *(bool)*
  - **Music** *(dict)* : `background_music`, `dj`, `jukebox`, `karaoke`, `live`, `no_music`, `video` *(bool)*

---

## 2. Fichier `checkin.json`
Historique des pointages d'utilisateurs par établissement.
- `business_id` *(str)* : Identifiant de l'établissement
- `date` *(str)* : Chaîne de caractères listant les horodatages des visites

---

## 3. Fichier `tip.csv`
Conseils et astuces rapides rédigés par les utilisateurs.
- `business_id` *(str)* : Identifiant de l'établissement
- `user_id` *(str)* : Identifiant de l'auteur
- `date` *(str)* : Date de publication
- `text` *(str)* : Contenu textuel de la recommandation
- `compliment_count` *(int)* : Nombre de mentions positives reçues