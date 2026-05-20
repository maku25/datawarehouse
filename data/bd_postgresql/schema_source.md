#Schéma de la BD source (posgtresql)

Cette doc détaille la structure des tables sources pré-existantes dans la bd PostgreSQL avant l'extraction par le pipeline ETL.

### Table : `elite`
| Colonne | Type | Nullable |
| :--- | :--- | :--- |
| `user_id` | `text` | OUI |
| `year` | `integer` | OUI |

### Table : `friend`
| Colonne | Type | Nullable |
| :--- | :--- | :--- |
| `user_id` | `text` | OUI |
| `friend_id` | `text` | OUI |
| `spark_partition`| `integer` | OUI |

### Table : `review`
| Colonne | Type | Nullable |
| :--- | :--- | :--- |
| `review_id` | `text` | OUI |
| `business_id` | `text` | OUI |
| `user_id` | `text` | OUI |
| `stars` | `double precision` | OUI |
| `date` | `date` | OUI |
| `text` | `text` | OUI |
| `useful` | `bigint` | OUI |
| `funny` | `bigint` | OUI |
| `cool` | `bigint` | OUI |
| `spark_partition`| `integer` | OUI |

### Table : `user`
*(colonnes principales)*
| Colonne | Type | Nullable |
| :--- | :--- | :--- |
| `user_id` | `text` | OUI |
| `name` | `text` | OUI |
| `review_count` | `bigint` | OUI |
| `yelping_since`| `date` | OUI |
| `average_stars`| `double precision` | OUI |
| `useful`, `funny`, `cool` | `bigint` | OUI |
| `compliment_*` *(11 colonnes)*| `bigint` | OUI |