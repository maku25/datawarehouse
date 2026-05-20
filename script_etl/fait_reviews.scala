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
    val pgUser = sys.env("PG_USER")
    val pgPassword = sys.env("PG_PASSWORD")

    val oracleUrl = "jdbc:oracle:thin:@//stendhal.iem:1521/enss2025"
    val oracleProps = new java.util.Properties()
    oracleProps.setProperty("user", oracleUser)
    oracleProps.setProperty("password", oraclePassword)
    oracleProps.setProperty("driver", "oracle.jdbc.OracleDriver")
    val pgUrl = "jdbc:postgresql://stendhal.iem:5432/tpid2020"

    // on charge les dimensions cible pour faire le filtre d'integrite referentielle
    // (un fait dont la cle pointe vers rien -> on le drop)
    println("Lecture dimensions depuis Oracle...")
    val dimCommerce = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_COMMERCE")
      .option("user", oracleUser).option("password", oraclePassword)
      .option("driver", "oracle.jdbc.OracleDriver").load()
      .select("COMMERCE_ID").cache()
    val dimUser = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_UTILISATEUR")
      .option("user", oracleUser).option("password", oraclePassword)
      .option("driver", "oracle.jdbc.OracleDriver").load()
      .select("USER_ID").cache()
    println(s"   -> ${dimCommerce.count()} commerces, ${dimUser.count()} utilisateurs")

    println("=== [6/8] fait_reviews ===")

    val dfReview = spark.read.format("jdbc")
      .option("url", pgUrl)
      .option("dbtable", "yelp.review")
      .option("user", pgUser).option("password", pgPassword)
      .load()

    val faitReviews = dfReview.select(
      $"review_id".as("REVIEW_ID"),
      $"business_id".as("COMMERCE_ID"),
      $"user_id".as("USER_ID"),
      // on convertit la date en cle technique yyyymmdd pour pointer sur dim_temps
      date_format($"date", "yyyyMMdd").cast("int").as("DATE_ID"),
      $"stars".as("NOTE"),
      $"useful".as("VOTES_UTILE"),
      $"funny".as("VOTES_DROLE"),
      $"cool".as("VOTES_COOL")
    )
    // inner join = on ne garde que les reviews dont le commerce ET le user existent en dim
    .join(dimCommerce, Seq("COMMERCE_ID"), "inner")
    .join(dimUser, Seq("USER_ID"), "inner")
    .dropDuplicates("REVIEW_ID")

    faitReviews.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_REVIEWS", oracleProps)
    println("   -> DONE")

    spark.stop()
  }
}