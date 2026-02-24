### DIM_COMMERCE

| Name | Null? | Type |
| :--- | :--- | :--- |
| **COMMERCE_ID** | NOT NULL | VARCHAR2(22) |
| **GEO_ID** | | NUMBER |
| **NOM** | NOT NULL | VARCHAR2(255) |
| **CATEGORIES** | | VARCHAR2(1000) |
| **EST_OUVERT** | | NUMBER(1) |
| **NOTE_MOYENNE_SOURCE** | | NUMBER(3,2) |
| **NB_AVIS_SOURCE** | | NUMBER(10) |
| **GAMME_PRIX** | | NUMBER(1) |
| **WIFI** | | VARCHAR2(20) |
| **ALCOOL** | | VARCHAR2(50) |
| **PARKING_GARAGE** | | NUMBER(1) |
| **PARKING_RUE** | | NUMBER(1) |
| **ACCEPTE_CB** | | NUMBER(1) |
| **TERRASSE** | | NUMBER(1) |
| **MENU_VEGE** | | NUMBER(1) |
| **ADAPTE_ENFANTS** | | NUMBER(1) |
| **LIVRAISON** | | NUMBER(1) |
| **VENTE_A_EMPORTER** | | NUMBER(1) |
| **ACCES_HANDICAPE** | | NUMBER(1) |

---

### DIM_GEOGRAPHIE

| Name | Null? | Type |
| :--- | :--- | :--- |
| **GEO_ID** | NOT NULL | NUMBER |
| **ADRESSE** | | VARCHAR2(4000) |
| **VILLE** | | VARCHAR2(255) |
| **ETAT** | | VARCHAR2(50) |
| **CODE_POSTAL** | | VARCHAR2(20) |
| **LATITUDE** | | NUMBER |
| **LONGITUDE** | | NUMBER |

---

### DIM_TEMPS

| Name | Null? | Type |
| :--- | :--- | :--- |
| **DATE_ID** | NOT NULL | NUMBER |
| **DATE_COMPLETE** | NOT NULL | DATE |
| **ANNEE** | NOT NULL | NUMBER(4) |
| **TRIMESTRE** | NOT NULL | NUMBER(1) |
| **MOIS** | NOT NULL | NUMBER(2) |
| **NOM_MOIS** | | VARCHAR2(20) |
| **JOUR** | NOT NULL | NUMBER(2) |
| **JOUR_SEMAINE** | | VARCHAR2(20) |
| **EST_WEEKEND** | | NUMBER(1) |

---

### DIM_UTILISATEUR

| Name | Null? | Type |
| :--- | :--- | :--- |
| **USER_ID** | NOT NULL | VARCHAR2(22) |
| **NOM** | | VARCHAR2(255) |
| **DATE_INSCRIPTION** | | DATE |
| **NB_AVIS_TOTAL** | | NUMBER(10) |
| **NB_FANS** | | NUMBER(10) |
| **NOTE_MOY_DONNEE** | | NUMBER(3,2) |
| **EST_ELITE** | | NUMBER(1) |
| **NB_ANNEES_ELITE** | | NUMBER(5) |

---

### FAIT_FLUX

| Name | Null? | Type |
| :--- | :--- | :--- |
| **COMMERCE_ID** | NOT NULL | VARCHAR2(22) |
| **DATE_ID** | NOT NULL | NUMBER |
| **NB_CHECKINS** | | NUMBER |

---

### FAIT_REVIEWS

| Name | Null? | Type |
| :--- | :--- | :--- |
| **REVIEW_ID** | NOT NULL | VARCHAR2(22) |
| **COMMERCE_ID** | NOT NULL | VARCHAR2(22) |
| **USER_ID** | NOT NULL | VARCHAR2(22) |
| **DATE_ID** | NOT NULL | NUMBER |
| **NOTE** | | NUMBER(1) |
| **VOTES_UTILE** | | NUMBER |
| **VOTES_DROLE** | | NUMBER |
| **VOTES_COOL** | | NUMBER |

---

### FAIT_TIPS

| Name | Null? | Type |
| :--- | :--- | :--- |
| **COMMERCE_ID** | NOT NULL | VARCHAR2(22) |
| **USER_ID** | NOT NULL | VARCHAR2(22) |
| **DATE_ID** | NOT NULL | NUMBER |
| **NB_COMPLIMENTS** | | NUMBER |
