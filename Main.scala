import org.apache.spark.sql.{SparkSession, SaveMode, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.jdbc.JdbcDialects

object Main {
  def main(args: Array[String]): Unit = {
    
    val spark = SparkSession.builder()
      .appName("Yelp_ETL_Master2_BDIA")
      .master("local[*]") 
      .getOrCreate()

    import spark.implicits._

    JdbcDialects.registerDialect(new OracleDialect)

    val login = "mb084205" 
    val oracleUrl = s"jdbc:oracle:thin:@//stendhal.iem:1521/enss2025"
    val oracleProps = new java.util.Properties()
    oracleProps.setProperty("user", login)
    oracleProps.setProperty("password", login)
    oracleProps.setProperty("driver", "oracle.jdbc.OracleDriver")

    val pgUrl = "jdbc:postgresql://stendhal.iem:5432/tpid2020"

    val basePath = "/data/M2BDIA-ID-Ressources/dataset/"
    val businessPath = basePath + "yelp_academic_dataset_business.json"
    val checkinPath = basePath + "yelp_academic_dataset_checkin.json"
    val tipPath = basePath + "yelp_academic_dataset_tip.csv"

    println("etl")

    println("traitement dim temps")
    val dimTemps = spark.sql("SELECT explode(sequence(to_date('2004-01-01'), to_date('2025-12-31'), interval 1 day)) as date_full")
      .select(
        date_format($"date_full", "yyyyMMdd").cast("int").as("DATE_ID"),
        $"date_full".as("DATE_COMPLETE"),
        year($"date_full").as("ANNEE"),
        quarter($"date_full").as("TRIMESTRE"),
        month($"date_full").as("MOIS"),
        date_format($"date_full", "MMMM").as("NOM_MOIS"),
        dayofmonth($"date_full").as("JOUR"),
        date_format($"date_full", "EEEE").as("JOUR_SEMAINE"),
        when(date_format($"date_full", "E").isin("Sat", "Sun"), 1).otherwise(0).as("EST_WEEKEND")
      ).distinct().cache()
    
    dimTemps.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_TEMPS", oracleProps)

    println("traitement dim geo")
    val rawBusiness = spark.read.json(businessPath).cache()
    
    val dimGeo = rawBusiness.select(
      $"address".as("ADRESSE"),
      $"city".as("VILLE"),
      $"state".as("ETAT"),
      $"postal_code".as("CODE_POSTAL"),
      $"latitude".as("LATITUDE"),
      $"longitude".as("LONGITUDE")
    ).distinct()
     .withColumn("GEO_ID", monotonically_increasing_id()).cache()

    dimGeo.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_GEOGRAPHIE", oracleProps)

    println("traitement dim commerce")
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
        when(get_json_object($"attributes.BusinessParking", "$.garage") === "True", 1).otherwise(0).as("PARKING_GARAGE"),
        when(get_json_object($"attributes.BusinessParking", "$.street") === "True", 1).otherwise(0).as("PARKING_RUE"),
        when($"attributes.BusinessAcceptsCreditCards" === "True", 1).otherwise(0).as("ACCEPTE_CB"),
        when($"attributes.HasTV" === "True", 1).otherwise(0).as("TERRASSE"),
        when(get_json_object($"attributes.Ambience", "$.romantic") === "True", 1).otherwise(0).as("MENU_VEGE"),
        when($"attributes.GoodForKids" === "True", 1).otherwise(0).as("ADAPTE_ENFANTS"),
        when($"attributes.RestaurantsDelivery" === "True", 1).otherwise(0).as("LIVRAISON"),
        when($"attributes.RestaurantsTakeOut" === "True", 1).otherwise(0).as("VENTE_A_EMPORTER"),
        when($"attributes.WheelchairAccessible" === "True", 1).otherwise(0).as("ACCES_HANDICAPE")
      )
      .filter(col("NOM").isNotNull && col("NOM") =!= "" && col("COMMERCE_ID").isNotNull)
      .dropDuplicates("COMMERCE_ID").cache()

    dimCommerce.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_COMMERCE", oracleProps)

    println("traitement dim utilisateur")
    val dfUserBase = spark.read.format("jdbc")
      .option("url", pgUrl).option("dbtable", "yelp.\"user\"") 
      .option("user", "tpid").option("password", "tpid").load()

    val dfEliteAgg = spark.read.format("jdbc")
      .option("url", pgUrl).option("dbtable", "yelp.elite")
      .option("user", "tpid").option("password", "tpid").load()
      .groupBy("user_id").agg(count("*").as("NB_ANNEES_ELITE"))

    val dimUser = dfUserBase.join(dfEliteAgg, Seq("user_id"), "left")
      .select(
        $"user_id".as("USER_ID"),
        $"name".as("NOM"),
        to_date($"yelping_since").as("DATE_INSCRIPTION"),
        $"review_count".as("NB_AVIS_TOTAL"),
        $"fans".as("NB_FANS"),
        $"average_stars".as("NOTE_MOY_DONNEE"),
        when($"NB_ANNEES_ELITE".isNotNull, 1).otherwise(0).as("EST_ELITE"),
        coalesce($"NB_ANNEES_ELITE", lit(0)).as("NB_ANNEES_ELITE")
      ).dropDuplicates("USER_ID").cache()

    dimUser.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_UTILISATEUR", oracleProps)

    println("traitement fait reviews")
    val dfReview = spark.read.format("jdbc")
      .option("url", pgUrl).option("dbtable", "yelp.review")
      .option("user", "tpid").option("password", "tpid").load()

    val faitReviews = dfReview.select(
      $"review_id".as("REVIEW_ID"),
      $"business_id".as("COMMERCE_ID"),
      $"user_id".as("USER_ID"),
      date_format($"date", "yyyyMMdd").cast("int").as("DATE_ID"),
      $"stars".as("NOTE"),
      $"useful".as("VOTES_UTILE"),
      $"funny".as("VOTES_DROLE"),
      $"cool".as("VOTES_COOL")
    )
    .join(dimCommerce.select("COMMERCE_ID"), Seq("COMMERCE_ID"), "inner")
    .join(dimUser.select("USER_ID"), Seq("USER_ID"), "inner")
    .dropDuplicates("REVIEW_ID")

    faitReviews.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_REVIEWS", oracleProps)

    println("traitement fait flux")
    val faitFlux = spark.read.json(checkinPath)
      .withColumn("date_str", explode(split($"date", ",")))
      .select(
        $"business_id".as("COMMERCE_ID"),
        date_format(trim($"date_str"), "yyyyMMdd").cast("int").as("DATE_ID")
      )
      .join(dimCommerce.select("COMMERCE_ID"), Seq("COMMERCE_ID"), "inner")
      .groupBy("COMMERCE_ID", "DATE_ID")
      .agg(count("*").as("NB_CHECKINS"))

    faitFlux.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_FLUX", oracleProps)

    println("traitement fait tips")
    val faitTips = spark.read.option("header", "true").csv(tipPath)
      .select(
        $"business_id".as("COMMERCE_ID"),
        $"user_id".as("USER_ID"),
        date_format(to_date($"date"), "yyyyMMdd").cast("int").as("DATE_ID"),
        $"compliment_count".cast("int").as("NB_COMPLIMENTS")
      )
      .join(dimCommerce.select("COMMERCE_ID"), Seq("COMMERCE_ID"), "inner")
      .join(dimUser.select("USER_ID"), Seq("USER_ID"), "inner")

    faitTips.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_TIPS", oracleProps)

    println("ETL terminé avec succès")
    spark.stop()
  }
}