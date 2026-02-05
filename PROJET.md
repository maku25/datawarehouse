# Feuille de Route 

## État Actuel : Février 2026
- [x] Accès serveurs Stendhal et Oracle validés.
- [x] Modélisation logique finalisée (Constellation).
- [x] Script SQL de création des tables exécuté sur `enss2025`.
- [ ] Développemenclt de la couche ETL Spark (Scala).
- [ ] Recette et validation des indicateurs (KPI).

---

## Procédure Technique (Rappel)
### Connexion à l'Entrepôt
* **Outil :** SQL Developer (`./sqldevelopper.sh`).
* **Hôte :** `enss2025.u-bourgogne.fr`
* **Login/Pass :** `mouad` / `mouad`
* **SID :** `enss2025`

### Environnement Stendhal
* **Commande Oracle :** `. /opt/oraenv.sh` (obligatoire avant toute opération SQL*Plus).
* **Spark :** Utilisation de Scala pour le traitement des sources `/data/M2BDIA-ID-Ressources/`.

---

## Planning des Tâches (To-Do)

| Priorité | Tâche | Description |
| :--- | :--- | :--- |
| 🔥 **Haute** | **Mapping ETL** | Lister chaque correspondance champ source -> colonne Oracle. |
| 🔥 **Haute** | **Chargement Dims** | Coder l'injection des tables DIM_GEO et DIM_COMMERCE. |
| ⚠️ **Moyenne** | **Calcul des Faits** | Agrégation des notes et calcul des indices de marché en Spark. |
| ✅ **Basse** | **Tests BI** | Requêtes SQL finales pour extraire les "pépites" pour l'investisseur. |

---
*Dernière mise à jour : 29 Janvier 2026*