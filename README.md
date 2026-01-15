Projet innformatique décisionnelle

étape 1 :
obj métier = identifier les opportunités d'implantation et les pépites à acheter

(on a pas les chiffres CA, benefs etc..)

étape 2 : 
approche kimball avec schéma en étoile

table de faits : FAIT_INVESTISSEMENT (mesures: notes, checkins, sentiment, votes)
4 dims : DIM_BUSINESS (service, prix), DIM_GEO (quartiers, loc gps), DIM_TEMPS (saisons, années), DIM_UTILISATEUR (élites, influenceurs) 

à faire: 
- script sql
- code spark (scala)

