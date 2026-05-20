import org.apache.spark.sql.{SparkSession, SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.jdbc.JdbcDialects

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("ETL").master("local[*]").getOrCreate()
    import spark.implicits._
    JdbcDialects.registerDialect(new OracleDialect)

    // credentials lus depuis l'environnement (cf .env)
    val oracleUser = sys.env("ORACLE_USER")
    val oraclePassword = sys.env("ORACLE_PASSWORD")

    val oracleUrl = "jdbc:oracle:thin:@//stendhal.iem:1521/enss2025"
    val oracleProps = new java.util.Properties()
    oracleProps.setProperty("user", oracleUser)
    oracleProps.setProperty("password", oraclePassword)
    oracleProps.setProperty("driver", "oracle.jdbc.OracleDriver")

    // on garde le json en cache parce qu'on va le re-utiliser pour la jointure
    val rawBusiness = spark.read.json("/data/M2BDIA-ID-Ressources/dataset/yelp_academic_dataset_business.json").cache()

    // on relit dim_geographie depuis oracle pour recuperer les GEO_ID
    // (pas le choix, c'est la qu'ils ont ete generes)
    println("Lecture DIM_GEOGRAPHIE depuis Oracle...")
    val dimGeo = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_GEOGRAPHIE")
      .option("user", oracleUser).option("password", oraclePassword)
      .option("driver", "oracle.jdbc.OracleDriver").load().cache()
    println(s"   -> ${dimGeo.count()} lignes en base")

    println("=== [3/8] dim_commerce ===")

    // gros select : on aplatit les attributs imbriques du json
    // (wifi, parking, etc sont dans des sous-objets, on les remonte en colonnes)
    val dimCommerce = rawBusiness.join(dimGeo,
        rawBusiness("address") === dimGeo("ADRESSE") &&
        rawBusiness("city") === dimGeo("VILLE") &&
        rawBusiness("state") === dimGeo("ETAT") &&
        rawBusiness("postal_code") === dimGeo("CODE_POSTAL")
      )
      .select(
        $"business_id".as("COMMERCE_ID"),
        $"GEO_ID",
        $"name".as("NOM"),
        $"categories",
        when($"is_open" === 1, 1).otherwise(0).as("EST_OUVERT"),
        $"stars".as("NOTE_MOYENNE_SOURCE"),
        $"review_count".as("NB_AVIS_SOURCE"),
        $"attributes.RestaurantsPriceRange2".cast("int").as("GAMME_PRIX"),
        $"attributes.WiFi".as("WIFI"),
        $"attributes.Alcohol".as("ALCOOL"),
        // BusinessParking est un json dans le json, on extrait les sous-cles
        when(get_json_object($"attributes.BusinessParking", "$.garage") === "True", 1).otherwise(0).as("PARKING_GARAGE"),
        when(get_json_object($"attributes.BusinessParking", "$.street") === "True", 1).otherwise(0).as("PARKING_RUE"),
        when($"attributes.BusinessAcceptsCreditCards" === "True", 1).otherwise(0).as("ACCEPTE_CB"),
        when($"attributes.OutdoorSeating" === "True", 1).otherwise(0).as("TERRASSE"),
        // menu vege : on regarde l'attribut explicite OU la categorie
        when(
          get_json_object($"attributes.DietaryRestrictions", "$.vegetarian") === "True"
          || $"categories".contains("Vegetarian")
          || $"categories".contains("Vegan"),
          1
        ).otherwise(0).as("MENU_VEGE"),
        when($"attributes.GoodForKids" === "True", 1).otherwise(0).as("ADAPTE_ENFANTS"),
        when($"attributes.RestaurantsDelivery" === "True", 1).otherwise(0).as("LIVRAISON"),
        when($"attributes.RestaurantsTakeOut" === "True", 1).otherwise(0).as("VENTE_A_EMPORTER"),
        when($"attributes.WheelchairAccessible" === "True", 1).otherwise(0).as("ACCES_HANDICAPE")
      )
      // on vire les commerces sans nom ou sans id, sinon c'est inutilisable
      .filter(col("NOM").isNotNull && col("NOM") =!= "" && col("COMMERCE_ID").isNotNull)
      .dropDuplicates("COMMERCE_ID")

    dimCommerce.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_COMMERCE", oracleProps)
    println(s"   -> DONE: ${dimCommerce.count()} lignes")

    spark.stop()
  }
}