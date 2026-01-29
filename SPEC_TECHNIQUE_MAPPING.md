# Mapping Source-Cible (ETL)

Ce document détaille les règles de passage entre les données brutes (JSON/PostgreSQL) et l'entrepôt de données Oracle (`enss2025`). Il sert de référence pour le développement des scripts Spark/Scala.

---

## 1. Mapping des Dimensions
Les dimensions sont chargées en premier. Elles ne nécessitent généralement pas d'agrégations complexes, mais du nettoyage de données.

### DIM_GEOGRAPHIE
| Colonne Oracle | Source | Champ Source | Transformation / Règle |
| :--- | :--- | :--- | :--- |
| `ADRESSE` | `business.json` | `address` | `trim()` |
| `VILLE` | `business.json` | `city` | `upper()` |
| `CODE_POSTAL` | `business.json` | `postal_code` | `substring(0, 5)` |
| `LATITUDE` | `business.json` | `latitude` | `cast(Double)` |
| `LONGUEUR` | `business.json` | `longitude` | `cast(Double)` |

### DIM_COMMERCE
| Colonne Oracle | Source | Champ Source | Transformation / Règle |
| :--- | :--- | :--- | :--- |
| `ID_COMMERCE` | `business.json` | `business_id` | Clé primaire (PK) |
| `NOM` | `business.json` | `name` | `trim()` |
| `GAMME_PRIX` | `business.json` | `attributes.RestaurantsPriceRange2` | Si `null` alors `0`, sinon `Int` |
| `PARKING` | `business.json` | `attributes.BusinessParking` | Si contient 'True' -> `1` sinon `0` |

---

## 2. Mapping des Tables de Faits

Les tables de faits nécessitent des jointures entre PostgreSQL et les fichiers JSON.

### FAIT_PERFORMANCE
* **Objectif :** Mesurer la qualité perçue d'un établissement.
* **Fréquence :** Mensuelle.

| Colonne Oracle | Source | Champ Source | Règle Métier |
| :--- | :--- | :--- | :--- |
| `ID_TEMPS` | Spark | `date` | Join avec DIM_TEMPS sur la date de l'avis |
| `NOTE_ETOILE` | Postgres | `stars` | Note brute de la table `review` |
| `LONGUEUR_AVIS` | Postgres | `text` | `length(text)` : Proxy de précision |
| `NB_VOTES_IMPACT`| Postgres | `useful`, `funny`, `cool` | `useful + funny + cool` : Proxy de viralité |

### FAIT_MARCHE_LOCAL
* **Objectif :** Identifier les opportunités d'implantation par zone.

| Colonne Oracle | Source | Champ Source | Règle Métier |
| :--- | :--- | :--- | :--- |
| `CUMUL_VISITES` | `checkin.json`| `date` | `count(dates)` groupé par `business_id` |
| `NB_ACTIFS` | `business.json` | `is_open` | `count` où `is_open = 1` par Code Postal |
| `INDICE_OPP` | Calculé | - | $Indice = \frac{TotalVisites}{NombreCommerces}$ |

---

## 3. Règles Transversales de Développement (Spark)

1.  **Gestion des Nulls :** Aucun champ de dimension ne doit être `null`. Utiliser des valeurs par défaut (`'Inconnu'`, `0`).
2.  **Format de Date :** Standardiser au format `yyyy-MM-dd` avant l'injection.
3.  **Mode d'écriture :** Utiliser `.mode("append")` pour ne pas écraser les données existantes lors des tests partiels.
4.  **Connecteur Oracle :** - URL : `jdbc:oracle:thin:@//enss2025.u-bourgogne.fr:1521/pdbenss`
    - Driver : `oracle.jdbc.driver.OracleDriver`