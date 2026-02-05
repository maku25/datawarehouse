yelp_academic_dataset_business.json

Colonnes principales :
['address', 'attributes', 'business_id', 'categories', 'city', 'hours', 'is_open', 'latitude', 'longitude', 'name', 'postal_code', 'review_count', 'stars', 'state']

Sous-attributs :
['AcceptsInsurance', 'Alcohol', 'Ambience', 'BYOB', 'BYOBCorkage', 'BestNights', 'BikeParking', 'BusinessAcceptsBitcoin', 'BusinessAcceptsCreditCards', 'BusinessParking', 'ByAppointmentOnly', 'Caters', 'CoatCheck', 'Corkage', 'DietaryRestrictions', 'DogsAllowed', 'DriveThru', 'GoodForDancing', 'GoodForKids', 'GoodForMeal', 'HairSpecializesIn', 'HappyHour', 'HasTV', 'Music', 'NoiseLevel', 'OutdoorSeating', 'RestaurantsAttire', 'RestaurantsDelivery', 'RestaurantsGoodForGroups', 'RestaurantsPriceRange2', 'RestaurantsReservations', 'RestaurantsTableService', 'RestaurantsTakeOut', 'Smoking', 'WheelchairAccessible', 'WiFi']


checkin.json : 
Colonnes principales :
['business_id', 'date']

Pas de sous attributs

tip.csv : 
business_id,compliment_count,date,text,user_id

POSTGRESQL

yelp.review
business_id text  
cool bigint  
date date     
funny bigint     
review_id text          
stars double precision 
text text            
useful bigint           
user_id text         
spark_partition integer 
Index :
"review_spark_partition_idx" btree (spark_partition)

yelp.user :
average_stars      double precision
compliment_cool    bigint          
compliment_cute    bigint          
compliment_funny   bigint          
compliment_hot     bigint          
compliment_list    bigint          
compliment_more    bigint 
compliment_note    bigint 
compliment_photos  bigint 
compliment_plain   bigint 
compliment_profile bigint 
compliment_writer  bigint 
cool               bigint 
fans               bigint 
funny              bigint 
name               text   
review_count       bigint 
useful             bigint 
user_id            text   
yelping_since      date    

yelp.elite : 
user_id text    
year integer

yelp.friend
user_id         text    
friend_id       text    
spark_partition integer 
Index :
"friend_spark_partition_idx" btree (spark_partition)
"friend_user_id_idx" btree (user_id)

