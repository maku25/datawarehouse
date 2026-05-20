heatmap : 
SELECT 
    G.ETAT, 
    SUM(F.NB_CHECKINS) AS TOTAL_VISITES
FROM FAIT_FLUX F
JOIN DIM_COMMERCE C ON F.COMMERCE_ID = C.COMMERCE_ID
JOIN DIM_GEOGRAPHIE G ON C.GEO_ID = G.GEO_ID
GROUP BY G.ETAT
ORDER BY TOTAL_VISITES DESC

volume de visites par ville :
SELECT 
    g.VILLE, g.ETAT,
    ROUND(AVG(g.LATITUDE), 4) AS LATITUDE,
    ROUND(AVG(g.LONGITUDE), 4) AS LONGITUDE,
    COUNT(DISTINCT c.COMMERCE_ID) AS NB_RESTAURANTS,
    NVL(SUM(f.NB_CHECKINS), 0) AS TOTAL_VISITES
FROM DIM_GEOGRAPHIE g
JOIN DIM_COMMERCE c ON g.GEO_ID = c.GEO_ID
LEFT JOIN FAIT_FLUX f ON c.COMMERCE_ID = f.COMMERCE_ID
WHERE c.CATEGORIES LIKE '%Restaurants%'
GROUP BY g.VILLE, g.ETAT
HAVING COUNT(DISTINCT c.COMMERCE_ID) > 30
ORDER BY TOTAL_VISITES DESC


top 5 villes : 
SELECT 
    g.VILLE, g.ETAT,
    ROUND(AVG(g.LATITUDE), 4) AS LATITUDE,
    ROUND(AVG(g.LONGITUDE), 4) AS LONGITUDE,
    COUNT(DISTINCT c.COMMERCE_ID) AS NB_RESTAURANTS,
    NVL(SUM(f.NB_CHECKINS), 0) AS TOTAL_VISITES
FROM DIM_GEOGRAPHIE g
JOIN DIM_COMMERCE c ON g.GEO_ID = c.GEO_ID
LEFT JOIN FAIT_FLUX f ON c.COMMERCE_ID = f.COMMERCE_ID
WHERE c.CATEGORIES LIKE '%Restaurants%'
GROUP BY g.VILLE, g.ETAT
HAVING COUNT(DISTINCT c.COMMERCE_ID) > 30
ORDER BY TOTAL_VISITES DESC
FETCH FIRST 5 ROWS ONLY


Top 15 villes par Indice de Tension (restaurants)

SELECT 
    g.VILLE, g.ETAT,
    COUNT(DISTINCT c.COMMERCE_ID) AS NB_RESTAURANTS,
    COUNT(r.REVIEW_ID) AS DEMANDE,
    ROUND(AVG(r.NOTE), 2) AS NOTE_MOY,
    ROUND(COUNT(r.REVIEW_ID) * 1.0 / NULLIF(COUNT(DISTINCT c.COMMERCE_ID), 0), 1) AS DENSITE,
    ROUND(
        (COUNT(r.REVIEW_ID) * 1.0 / NULLIF(COUNT(DISTINCT c.COMMERCE_ID), 0)) 
        * (5 - AVG(r.NOTE)), 
    2) AS INDICE_TENSION
FROM DIM_GEOGRAPHIE g
JOIN DIM_COMMERCE c ON g.GEO_ID = c.GEO_ID
JOIN FAIT_REVIEWS r ON c.COMMERCE_ID = r.COMMERCE_ID
WHERE c.CATEGORIES LIKE '%Restaurants%'
GROUP BY g.VILLE, g.ETAT
HAVING COUNT(DISTINCT c.COMMERCE_ID) > 30
ORDER BY INDICE_TENSION DESC
FETCH FIRST 15 ROWS ONLY


Volume par type de cuisine
SELECT 
    FAMILLE_CUISINE,
    COUNT(DISTINCT COMMERCE_ID) AS NB_RESTAURANTS,
    ROUND(SUM(NB_CHECKINS) * 1.0 / NULLIF(COUNT(DISTINCT COMMERCE_ID), 0), 0) AS VISITES_PAR_RESTAURANT,
    ROUND(AVG(NOTE_MOYENNE_SOURCE), 2) AS NOTE_MOY
FROM (
    SELECT DISTINCT
        c.COMMERCE_ID,
        c.NOTE_MOYENNE_SOURCE,
        NVL(f.NB_CHECKINS, 0) AS NB_CHECKINS,
        CASE 
            WHEN cat.NOM_CATEGORIE IN ('American (New)', 'American (Traditional)', 'Comfort Food', 'Southern', 'Soul Food') THEN 'Americain'
            WHEN cat.NOM_CATEGORIE IN ('Italian', 'Pizza', 'Sicilian', 'Tuscan') THEN 'Italien / Pizza'
            WHEN cat.NOM_CATEGORIE IN ('Mexican', 'Tex-Mex', 'Tacos', 'Latin American', 'Caribbean') THEN 'Latino / Mexicain'
            WHEN cat.NOM_CATEGORIE IN ('Chinese', 'Cantonese', 'Dim Sum', 'Noodles') THEN 'Chinois'
            WHEN cat.NOM_CATEGORIE IN ('Japanese', 'Sushi Bars', 'Ramen', 'Izakaya') THEN 'Japonais / Sushi'
            WHEN cat.NOM_CATEGORIE IN ('Thai', 'Vietnamese', 'Korean', 'Asian Fusion') THEN 'Asiatique autre'
            WHEN cat.NOM_CATEGORIE IN ('Indian', 'Pakistani') THEN 'Indien / Pakistanais'
            WHEN cat.NOM_CATEGORIE IN ('Mediterranean', 'Greek', 'Lebanese', 'Turkish', 'Middle Eastern') THEN 'Mediterraneen / Oriental'
            WHEN cat.NOM_CATEGORIE IN ('French') THEN 'Francais'
            WHEN cat.NOM_CATEGORIE IN ('Bistros', 'Creperies') THEN 'Bistros'
            WHEN cat.NOM_CATEGORIE IN ('Creperies') THEN 'Creperies'
            WHEN cat.NOM_CATEGORIE IN ('Seafood') THEN 'Fruits de mer'
            WHEN cat.NOM_CATEGORIE IN ('Fish & Chips') THEN 'Fish & Chips'
            WHEN cat.NOM_CATEGORIE IN ('Steakhouses', 'Barbeque', 'BBQ') THEN 'Grill / BBQ / Steakhouse'
            WHEN cat.NOM_CATEGORIE IN ('Burgers', 'Fast Food', 'Chicken Wings') THEN 'Fast Food / Burgers'
            WHEN cat.NOM_CATEGORIE IN ('Sandwiches', 'Salad', 'Soup') THEN 'Sandwiches / Salades'
            WHEN cat.NOM_CATEGORIE IN ('Breakfast & Brunch', 'Brunch') THEN 'Brunch / Petit-dej'
            WHEN cat.NOM_CATEGORIE IN ('Coffee & Tea', 'Cafes', 'Bakeries', 'Desserts', 'Ice Cream & Frozen Yogurt') THEN 'Cafes / Desserts'
            WHEN cat.NOM_CATEGORIE IN ('Bars', 'Pubs', 'Lounges') THEN 'Bars'
            WHEN cat.NOM_CATEGORIE IN ('Nightlife') THEN 'Nightlife'
            WHEN cat.NOM_CATEGORIE IN ('Vegan', 'Vegetarian', 'Gluten-Free', 'Healthy') THEN 'Veggie / Healthy'
            ELSE 'Autres'
        END AS FAMILLE_CUISINE
    FROM DIM_COMMERCE c
    JOIN PONT_COMMERCE_CATEGORIE pcc ON c.COMMERCE_ID = pcc.COMMERCE_ID
    JOIN DIM_CATEGORIE cat ON pcc.CATEGORIE_ID = cat.CATEGORIE_ID
    LEFT JOIN FAIT_FLUX f ON c.COMMERCE_ID = f.COMMERCE_ID
    WHERE c.CATEGORIES LIKE '%Restaurants%'
      AND cat.NOM_CATEGORIE NOT IN ('Restaurants', 'Food')
) sub
WHERE FAMILLE_CUISINE <> 'Autres'
GROUP BY FAMILLE_CUISINE
ORDER BY VISITES_PAR_RESTAURANT DESC



Top 10 villes × meilleure famille cuisine
SELECT VILLE, ETAT, FAMILLE_CUISINE, NOTE_MOY, NB_RESTAURANTS, INDICE_TENSION
FROM (
    SELECT 
        g.VILLE, g.ETAT, sub.FAMILLE_CUISINE,
        ROUND(AVG(r.NOTE), 2) AS NOTE_MOY,
        COUNT(DISTINCT c.COMMERCE_ID) AS NB_RESTAURANTS,
        ROUND(
            (COUNT(r.REVIEW_ID) * 1.0 / NULLIF(COUNT(DISTINCT c.COMMERCE_ID), 0)) 
            * (5 - AVG(r.NOTE)), 
        2) AS INDICE_TENSION,
        ROW_NUMBER() OVER (PARTITION BY g.VILLE ORDER BY 
            (COUNT(r.REVIEW_ID) * 1.0 / NULLIF(COUNT(DISTINCT c.COMMERCE_ID), 0)) * (5 - AVG(r.NOTE)) DESC
        ) AS RANG
    FROM DIM_GEOGRAPHIE g
    JOIN DIM_COMMERCE c ON g.GEO_ID = c.GEO_ID
    JOIN PONT_COMMERCE_CATEGORIE pcc ON c.COMMERCE_ID = pcc.COMMERCE_ID
    JOIN DIM_CATEGORIE cat ON pcc.CATEGORIE_ID = cat.CATEGORIE_ID
    JOIN FAIT_REVIEWS r ON c.COMMERCE_ID = r.COMMERCE_ID
    JOIN (
        SELECT 'Americain' AS FAMILLE_CUISINE, 'American (New)' AS NOM FROM DUAL UNION ALL
        SELECT 'Americain', 'American (Traditional)' FROM DUAL UNION ALL
        SELECT 'Americain', 'Comfort Food' FROM DUAL UNION ALL
        SELECT 'Italien / Pizza', 'Italian' FROM DUAL UNION ALL
        SELECT 'Italien / Pizza', 'Pizza' FROM DUAL UNION ALL
        SELECT 'Latino / Mexicain', 'Mexican' FROM DUAL UNION ALL
        SELECT 'Latino / Mexicain', 'Tex-Mex' FROM DUAL UNION ALL
        SELECT 'Latino / Mexicain', 'Tacos' FROM DUAL UNION ALL
        SELECT 'Latino / Mexicain', 'Caribbean' FROM DUAL UNION ALL
        SELECT 'Chinois', 'Chinese' FROM DUAL UNION ALL
        SELECT 'Chinois', 'Cantonese' FROM DUAL UNION ALL
        SELECT 'Japonais / Sushi', 'Japanese' FROM DUAL UNION ALL
        SELECT 'Japonais / Sushi', 'Sushi Bars' FROM DUAL UNION ALL
        SELECT 'Japonais / Sushi', 'Ramen' FROM DUAL UNION ALL
        SELECT 'Asiatique autre', 'Thai' FROM DUAL UNION ALL
        SELECT 'Asiatique autre', 'Vietnamese' FROM DUAL UNION ALL
        SELECT 'Asiatique autre', 'Korean' FROM DUAL UNION ALL
        SELECT 'Asiatique autre', 'Asian Fusion' FROM DUAL UNION ALL
        SELECT 'Indien / Pakistanais', 'Indian' FROM DUAL UNION ALL
        SELECT 'Indien / Pakistanais', 'Pakistani' FROM DUAL UNION ALL
        SELECT 'Mediterraneen / Oriental', 'Mediterranean' FROM DUAL UNION ALL
        SELECT 'Mediterraneen / Oriental', 'Greek' FROM DUAL UNION ALL
        SELECT 'Mediterraneen / Oriental', 'Middle Eastern' FROM DUAL UNION ALL
        SELECT 'Francais / Belge', 'French' FROM DUAL UNION ALL
        SELECT 'Fruits de mer', 'Seafood' FROM DUAL UNION ALL
        SELECT 'Grill / BBQ', 'Steakhouses' FROM DUAL UNION ALL
        SELECT 'Grill / BBQ', 'Barbeque' FROM DUAL UNION ALL
        SELECT 'Fast Food / Burgers', 'Burgers' FROM DUAL UNION ALL
        SELECT 'Fast Food / Burgers', 'Fast Food' FROM DUAL UNION ALL
        SELECT 'Fast Food / Burgers', 'Chicken Wings' FROM DUAL UNION ALL
        SELECT 'Brunch / Petit-dej', 'Breakfast & Brunch' FROM DUAL UNION ALL
        SELECT 'Sandwiches / Salades', 'Sandwiches' FROM DUAL UNION ALL
        SELECT 'Sandwiches / Salades', 'Salad' FROM DUAL
    ) sub ON cat.NOM_CATEGORIE = sub.NOM
    WHERE c.CATEGORIES LIKE '%Restaurants%'
    GROUP BY g.VILLE, g.ETAT, sub.FAMILLE_CUISINE
    HAVING COUNT(DISTINCT c.COMMERCE_ID) >= 5 AND COUNT(r.REVIEW_ID) >= 30
)
WHERE RANG = 1
ORDER BY INDICE_TENSION DESC
FETCH FIRST 10 ROWS ONLY


impact des services sur la note moyenne
WITH ServiceStats AS (
    -- Services Binaires (0/1)
    SELECT 'Accepte CB' AS SERVICE, CASE WHEN ACCEPTE_CB = 1 THEN 'OUI' ELSE 'NON' END AS DISPO, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Terrasse', CASE WHEN TERRASSE = 1 THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Menu Végé', CASE WHEN MENU_VEGE = 1 THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Adapté Enfants', CASE WHEN ADAPTE_ENFANTS = 1 THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Livraison', CASE WHEN LIVRAISON = 1 THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Vente à emporter', CASE WHEN VENTE_A_EMPORTER = 1 THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Accès Handicapé', CASE WHEN ACCES_HANDICAPE = 1 THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    -- Services Textuels (Transformation en OUI/NON)
    UNION ALL
    SELECT 'WiFi', CASE WHEN (WIFI IS NOT NULL AND WIFI <> 'no') THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
    UNION ALL
    SELECT 'Alcool', CASE WHEN (ALCOOL IS NOT NULL AND ALCOOL <> 'none') THEN 'OUI' ELSE 'NON' END, NOTE_MOYENNE_SOURCE FROM DIM_COMMERCE WHERE (CATEGORIES LIKE '%Restaurants%' OR CATEGORIES LIKE '%Food%')
)
SELECT 
    SERVICE, 
    DISPO, 
    ROUND(AVG(NOTE_MOYENNE_SOURCE), 2) AS NOTE_MOYENNE
FROM ServiceStats
GROUP BY SERVICE, DISPO
ORDER BY SERVICE, DISPO DESC


Top 5 national vs Top 5 même état
SELECT CLASSEMENT, RANG, NOM, VILLE, ETAT, NOTE_MOY, NB_AVIS, 
    "Terrasse", "Livraison", "WiFi", "Menu Végé", 
    "Accès Handicapé", "Adapté Enfants", "Vente à emporter"
FROM (
    -- TOP 5 NATIONAL
    SELECT '1. TOP NATIONAL' AS CLASSEMENT,
        ROW_NUMBER() OVER (ORDER BY AVG(r.NOTE) DESC) AS RANG,
        c.NOM, g.VILLE, g.ETAT,
        ROUND(AVG(r.NOTE), 2) AS NOTE_MOY,
        COUNT(r.REVIEW_ID) AS NB_AVIS,
        MAX(c.TERRASSE) AS "Terrasse",
        MAX(c.LIVRAISON) AS "Livraison",
        MAX(CASE WHEN c.WIFI LIKE '%free%' THEN 1 ELSE 0 END) AS "WiFi",
        MAX(c.MENU_VEGE) AS "Menu Végé",
        MAX(c.ACCES_HANDICAPE) AS "Accès Handicapé",
        MAX(c.ADAPTE_ENFANTS) AS "Adapté Enfants",
        MAX(c.VENTE_A_EMPORTER) AS "Vente à emporter"
    FROM DIM_COMMERCE c
    JOIN DIM_GEOGRAPHIE g ON c.GEO_ID = g.GEO_ID
    JOIN FAIT_REVIEWS r ON c.COMMERCE_ID = r.COMMERCE_ID
    WHERE (c.CATEGORIES LIKE '%Japanese%' OR c.CATEGORIES LIKE '%Sushi%' OR c.CATEGORIES LIKE '%Ramen%')
      AND c.CATEGORIES LIKE '%Restaurants%'
    GROUP BY c.NOM, g.VILLE, g.ETAT
    HAVING COUNT(r.REVIEW_ID) >= 20
)
WHERE RANG <= 5

UNION ALL

SELECT CLASSEMENT, RANG, NOM, VILLE, ETAT, NOTE_MOY, NB_AVIS,
    "Terrasse", "Livraison", "WiFi", "Menu Végé", 
    "Accès Handicapé", "Adapté Enfants", "Vente à emporter"
FROM (
    -- TOP 5 ARIZONA
    SELECT '2. TOP ARIZONA' AS CLASSEMENT,
        ROW_NUMBER() OVER (ORDER BY AVG(r.NOTE) DESC) AS RANG,
        c.NOM, g.VILLE, g.ETAT,
        ROUND(AVG(r.NOTE), 2) AS NOTE_MOY,
        COUNT(r.REVIEW_ID) AS NB_AVIS,
        MAX(c.TERRASSE) AS "Terrasse",
        MAX(c.LIVRAISON) AS "Livraison",
        MAX(CASE WHEN c.WIFI LIKE '%free%' THEN 1 ELSE 0 END) AS "WiFi",
        MAX(c.MENU_VEGE) AS "Menu Végé",
        MAX(c.ACCES_HANDICAPE) AS "Accès Handicapé",
        MAX(c.ADAPTE_ENFANTS) AS "Adapté Enfants",
        MAX(c.VENTE_A_EMPORTER) AS "Vente à emporter"
    FROM DIM_COMMERCE c
    JOIN DIM_GEOGRAPHIE g ON c.GEO_ID = g.GEO_ID
    JOIN FAIT_REVIEWS r ON c.COMMERCE_ID = r.COMMERCE_ID
    WHERE (c.CATEGORIES LIKE '%Japanese%' OR c.CATEGORIES LIKE '%Sushi%' OR c.CATEGORIES LIKE '%Ramen%')
      AND c.CATEGORIES LIKE '%Restaurants%'
      AND g.ETAT = 'AZ'
    GROUP BY c.NOM, g.VILLE, g.ETAT
    HAVING COUNT(r.REVIEW_ID) >= 10
)
WHERE RANG <= 5

ORDER BY CLASSEMENT, RANG


Corrélation nombre de services vs note
SELECT 
    NB_SERVICES,
    COUNT(*) AS NB_RESTAURANTS,
    ROUND(AVG(NOTE_MOYENNE_SOURCE), 2) AS NOTE_MOYENNE
FROM (
    SELECT COMMERCE_ID, NOTE_MOYENNE_SOURCE,
        NVL(CASE WHEN WIFI LIKE '%free%' THEN 1 ELSE 0 END, 0)
        + NVL(TERRASSE, 0) + NVL(LIVRAISON, 0) + NVL(VENTE_A_EMPORTER, 0)
        + NVL(PARKING_GARAGE, 0) + NVL(PARKING_RUE, 0)
        + NVL(ACCES_HANDICAPE, 0) + NVL(MENU_VEGE, 0)
        + NVL(ADAPTE_ENFANTS, 0) + NVL(ACCEPTE_CB, 0) AS NB_SERVICES
    FROM DIM_COMMERCE
    WHERE CATEGORIES LIKE '%Restaurants%'
)
GROUP BY NB_SERVICES
ORDER BY NB_SERVICES


commment les services varient selon le niveau de prix du restaurant
SELECT 
    CASE GAMME_PRIX 
        WHEN 1 THEN '1 - Cheap'
        WHEN 2 THEN '2 - Medium'
        WHEN 3 THEN '3 - Upscale'
        WHEN 4 THEN '4 - Premium'
    END AS GAMME,
    COUNT(*) AS NB_RESTAURANTS,
    ROUND(AVG(NOTE_MOYENNE_SOURCE), 2) AS NOTE_MOY,
    ROUND(SUM(CASE WHEN WIFI LIKE '%free%' THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS PCT_WIFI,
    ROUND(SUM(TERRASSE) * 100.0 / COUNT(*), 1) AS PCT_TERRASSE,
    ROUND(SUM(LIVRAISON) * 100.0 / COUNT(*), 1) AS PCT_LIVRAISON,
    ROUND(SUM(ACCES_HANDICAPE) * 100.0 / COUNT(*), 1) AS PCT_HANDICAPE
FROM DIM_COMMERCE
WHERE CATEGORIES LIKE '%Restaurants%' AND GAMME_PRIX IS NOT NULL
GROUP BY GAMME_PRIX
ORDER BY GAMME_PRIX


Répartition des profils
SELECT * FROM V_DASHBOARD_PROFILS
ORDER BY PROFIL_CLIENT


Impact moyen par avis selon le profil
SELECT * FROM V_DASHBOARD_PROFILS
ORDER BY PROFIL_CLIENT


Profil × type de cuisine préféré
SELECT PROFIL, FAMILLE_CUISINE, NB_AVIS, NOTE_MOY
FROM (
    SELECT 
        CASE 
            WHEN u.EST_ELITE = 1 THEN '1. ELITE'
            WHEN u.NB_AVIS_TOTAL > 50 THEN '2. EXPERT'
            ELSE '3. OCCASIONNEL'
        END AS PROFIL,
        CASE 
            WHEN cat.NOM_CATEGORIE IN ('American (New)', 'American (Traditional)', 'Comfort Food') THEN 'Americain'
            WHEN cat.NOM_CATEGORIE IN ('Italian', 'Pizza') THEN 'Italien / Pizza'
            WHEN cat.NOM_CATEGORIE IN ('Mexican', 'Tex-Mex', 'Tacos') THEN 'Latino / Mexicain'
            WHEN cat.NOM_CATEGORIE IN ('Chinese', 'Cantonese', 'Szechuan') THEN 'Chinois'
            WHEN cat.NOM_CATEGORIE IN ('Japanese', 'Sushi Bars', 'Ramen') THEN 'Japonais / Sushi'
            WHEN cat.NOM_CATEGORIE IN ('Thai', 'Vietnamese', 'Korean', 'Asian Fusion') THEN 'Asiatique autre'
            WHEN cat.NOM_CATEGORIE IN ('Indian', 'Pakistani') THEN 'Indien'
            WHEN cat.NOM_CATEGORIE IN ('Mediterranean', 'Greek', 'Middle Eastern') THEN 'Mediterraneen'
            WHEN cat.NOM_CATEGORIE IN ('Steakhouses', 'Barbeque') THEN 'Grill / BBQ'
            WHEN cat.NOM_CATEGORIE IN ('Burgers', 'Fast Food', 'Chicken Wings') THEN 'Fast Food'
            WHEN cat.NOM_CATEGORIE IN ('Breakfast & Brunch') THEN 'Brunch'
            WHEN cat.NOM_CATEGORIE IN ('Seafood') THEN 'Fruits de mer'
            ELSE NULL
        END AS FAMILLE_CUISINE,
        COUNT(r.REVIEW_ID) AS NB_AVIS,
        ROUND(AVG(r.NOTE), 2) AS NOTE_MOY,
        ROW_NUMBER() OVER (
            PARTITION BY CASE 
                WHEN u.EST_ELITE = 1 THEN '1. ELITE'
                WHEN u.NB_AVIS_TOTAL > 50 THEN '2. EXPERT'
                ELSE '3. OCCASIONNEL'
            END 
            ORDER BY COUNT(r.REVIEW_ID) DESC
        ) AS RANG
    FROM FAIT_REVIEWS r
    JOIN DIM_UTILISATEUR u ON r.USER_ID = u.USER_ID
    JOIN DIM_COMMERCE c ON r.COMMERCE_ID = c.COMMERCE_ID
    JOIN PONT_COMMERCE_CATEGORIE pcc ON c.COMMERCE_ID = pcc.COMMERCE_ID
    JOIN DIM_CATEGORIE cat ON pcc.CATEGORIE_ID = cat.CATEGORIE_ID
    WHERE c.CATEGORIES LIKE '%Restaurants%'
    GROUP BY 
        CASE 
            WHEN u.EST_ELITE = 1 THEN '1. ELITE'
            WHEN u.NB_AVIS_TOTAL > 50 THEN '2. EXPERT'
            ELSE '3. OCCASIONNEL'
        END,
        CASE 
            WHEN cat.NOM_CATEGORIE IN ('American (New)', 'American (Traditional)', 'Comfort Food') THEN 'Americain'
            WHEN cat.NOM_CATEGORIE IN ('Italian', 'Pizza') THEN 'Italien / Pizza'
            WHEN cat.NOM_CATEGORIE IN ('Mexican', 'Tex-Mex', 'Tacos') THEN 'Latino / Mexicain'
            WHEN cat.NOM_CATEGORIE IN ('Chinese', 'Cantonese', 'Szechuan') THEN 'Chinois'
            WHEN cat.NOM_CATEGORIE IN ('Japanese', 'Sushi Bars', 'Ramen') THEN 'Japonais / Sushi'
            WHEN cat.NOM_CATEGORIE IN ('Thai', 'Vietnamese', 'Korean', 'Asian Fusion') THEN 'Asiatique autre'
            WHEN cat.NOM_CATEGORIE IN ('Indian', 'Pakistani') THEN 'Indien'
            WHEN cat.NOM_CATEGORIE IN ('Mediterranean', 'Greek', 'Middle Eastern') THEN 'Mediterraneen'
            WHEN cat.NOM_CATEGORIE IN ('Steakhouses', 'Barbeque') THEN 'Grill / BBQ'
            WHEN cat.NOM_CATEGORIE IN ('Burgers', 'Fast Food', 'Chicken Wings') THEN 'Fast Food'
            WHEN cat.NOM_CATEGORIE IN ('Breakfast & Brunch') THEN 'Brunch'
            WHEN cat.NOM_CATEGORIE IN ('Seafood') THEN 'Fruits de mer'
            ELSE NULL
        END
    HAVING COUNT(r.REVIEW_ID) >= 50
)
WHERE FAMILLE_CUISINE IS NOT NULL AND RANG <= 5
ORDER BY PROFIL, RANG


KPI comparatifs
SELECT PROFIL_CLIENT, NB_CLIENTS, NOTE_MOYENNE_DONNEE, MOYENNE_FANS, IMPACT_MOYEN
FROM V_DASHBOARD_PROFILS
ORDER BY PROFIL_CLIENT

Courbes volume + note par mois
SELECT * FROM V_DASHBOARD_SAISONNALITE
ORDER BY MOIS



Check-ins par mois
SELECT * FROM V_DASHBOARD_SAISONNALITE
ORDER BY MOIS


Score de saisonnalité(le meilleur mois pour ouvrir)
SELECT NOM_MOIS, SCORE_SAISONNALITE,
    RANK() OVER (ORDER BY SCORE_SAISONNALITE DESC) AS RANG
FROM V_DASHBOARD_SAISONNALITE
ORDER BY SCORE_SAISONNALITE DESC

Weekend vs Semaine
SELECT 
    CASE WHEN t.EST_WEEKEND = 1 THEN 'WEEKEND' ELSE 'SEMAINE' END AS PERIODE,
    COUNT(r.REVIEW_ID) AS NB_AVIS,
    ROUND(AVG(r.NOTE), 3) AS NOTE_MOYENNE,
    ROUND(SUM(CASE WHEN r.NOTE = 5 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS PCT_5_ETOILES,
    ROUND(SUM(CASE WHEN r.NOTE = 1 THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 1) AS PCT_1_ETOILE
FROM FAIT_REVIEWS r
JOIN DIM_TEMPS t ON r.DATE_ID = t.DATE_ID
JOIN DIM_COMMERCE c ON r.COMMERCE_ID = c.COMMERCE_ID
WHERE c.CATEGORIES LIKE '%Restaurants%'
GROUP BY CASE WHEN t.EST_WEEKEND = 1 THEN 'WEEKEND' ELSE 'SEMAINE' END

