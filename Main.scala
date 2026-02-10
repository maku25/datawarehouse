import org.apache.spark.sql.{SparkSession, SaveMode, DataFrame}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.jdbc.JdbcDialects

object Main {
  def main(args: Array[String]): Unit = {
    
    val spark = SparkSession.builder()
      .appName("yelp_etl")
      .getOrCreate()

    import spark.implicits._

    JdbcDialects.registerDialect(new OracleDialect)

    val login = "mb084205" 
    val oracleUrl = s"jdbc:oracle:thin:@//stendhal.iem:1521/enss2025"
    val oracleProps = new java.util.Properties()
    oracleProps.setProperty("user", login)
    oracleProps.setProperty("password", login)
    oracleProps.setProperty("driver", "oracle.jdbc.OracleDriver")

    val pgUrl = "jdbc:postgresql://stendhal.iem:5432/yelp"
    val pgProps = new java.util.Properties()
    pgProps.setProperty("user", "tpid")
    pgProps.setProperty("password", "tpid")
    pgProps.setProperty("driver", "org.postgresql.Driver")

    val basePath = "/data/M2BDIA-ID-Ressources/dataset/"
    val businessPath = basePath + "business.json"
    val checkinPath = basePath + "checkin.json"
    val tipPath = basePath + "tip.csv"

    println("etl")

    //dim temps
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
      )
    dimTemps.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_TEMPS", oracleProps)

    // dim geo
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
     .withColumn("GEO_ID", monotonically_increasing_id())

    dimGeo.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_GEOGRAPHIE", oracleProps)

    // dim commerce 
    println("traitement dim commerce")
    val dimCommerce = rawBusiness.join(dimGeo, Seq("address", "city", "state", "postal_code"))
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
        when($"attributes.BusinessParking.garage" === "True", 1).otherwise(0).as("PARKING_GARAGE"),
        when($"attributes.BusinessParking.street" === "True", 1).otherwise(0).as("PARKING_RUE"),
        when($"attributes.BusinessAcceptsCreditCards" === "True", 1).otherwise(0).as("ACCEPTE_CB"),
        when($"attributes.HasTV" === "True", 1).otherwise(0).as("TERRASSE"), // Exemple
        when($"attributes.Ambience.romantic" === "True", 1).otherwise(0).as("MENU_VEGE"), // Adapté selon tes besoins
        when($"attributes.GoodForKids" === "True", 1).otherwise(0).as("ADAPTE_ENFANTS"),
        when($"attributes.RestaurantsDelivery" === "True", 1).otherwise(0).as("LIVRAISON"),
        when($"attributes.RestaurantsTakeOut" === "True", 1).otherwise(0).as("VENTE_A_EMPORTER"),
        when($"attributes.WheelchairAccessible" === "True", 1).otherwise(0).as("ACCES_HANDICAPE")
      )
    dimCommerce.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_COMMERCE", oracleProps)

    // dim utilisateur
    println("traitement dim utilisateur")
    val dfUser = spark.read.format("jdbc")
      .option("url", pgUrl)
      .option("dbtable", "\"user\"") // user est un mot réservé
      .option("user", "tpid").option("password", "tpid")
      .option("partitionColumn", "spark_partition")
      .option("lowerBound", "0").option("upperBound", "100")
      .option("numPartitions", "10")
      .load()

    val dimUser = dfUser.select(
      $"user_id".as("USER_ID"),
      $"name".as("NOM"),
      to_date($"yelping_since").as("DATE_INSCRIPTION"),
      $"review_count".as("NB_AVIS_TOTAL"),
      $"fans".as("NB_FANS"),
      $"average_stars".as("NOTE_MOY_DONNEE"),
      when($"elite".isNotNull && $"elite" =!= "", 1).otherwise(0).as("EST_ELITE"),
      size(split($"elite", ",")).as("NB_ANNEES_ELITE")
    )
    dimUser.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_UTILISATEUR", oracleProps)

    // fait review 
    println("traitemnt fait review")
    val dfReview = spark.read.format("jdbc")
      .option("url", pgUrl).option("dbtable", "review")
      .option("user", "tpid").option("password", "tpid")
      .option("partitionColumn", "spark_partition")
      .option("lowerBound", "0").option("upperBound", "100")
      .option("numPartitions", "10").load()

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
    faitReviews.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_REVIEWS", oracleProps)

    // fait flux 
    println("traitement fait flux")
    val faitFlux = spark.read.json(checkinPath)
      .withColumn("date_str", explode(split($"date", ",")))
      .select(
        $"business_id".as("COMMERCE_ID"),
        date_format(trim($"date_str"), "yyyyMMdd").cast("int").as("DATE_ID")
      )
      .groupBy("COMMERCE_ID", "DATE_ID")
      .agg(count("*").as("NB_CHECKINS"))

    faitFlux.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_FLUX", oracleProps)

    // fait tips
    println("traitement fait tips")
    val faitTips = spark.read.option("header", "true").csv(tipPath)
      .select(
        $"business_id".as("COMMERCE_ID"),
        $"user_id".as("USER_ID"),
        date_format(to_date($"date"), "yyyyMMdd").cast("int").as("DATE_ID"),
        $"compliment_count".cast("int").as("NB_COMPLIMENTS")
      )
    faitTips.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_TIPS", oracleProps)

    println("etl terminé")
    spark.stop()
  }
}