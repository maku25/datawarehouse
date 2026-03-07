import org.apache.spark.sql.{SparkSession, SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.jdbc.JdbcDialects

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("ETL").master("local[*]").getOrCreate()
    import spark.implicits._
    JdbcDialects.registerDialect(new OracleDialect)

    val login = "mb084205"
    val oracleUrl = "jdbc:oracle:thin:@//stendhal.iem:1521/enss2025"
    val oracleProps = new java.util.Properties()
    oracleProps.setProperty("user", login)
    oracleProps.setProperty("password", login)
    oracleProps.setProperty("driver", "oracle.jdbc.OracleDriver")
    val pgUrl = "jdbc:postgresql://stendhal.iem:5432/tpid2020"

    // Lire les dimensions depuis Oracle pour les jointures
    println("Lecture dimensions depuis Oracle...")
    val dimCommerce = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_COMMERCE")
      .option("user", login).option("password", login)
      .option("driver", "oracle.jdbc.OracleDriver").load()
      .select("COMMERCE_ID").cache()
    val dimUser = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_UTILISATEUR")
      .option("user", login).option("password", login)
      .option("driver", "oracle.jdbc.OracleDriver").load()
      .select("USER_ID").cache()
    println(s"   -> ${dimCommerce.count()} commerces, ${dimUser.count()} utilisateurs")

    println("=== [6/8] FAIT_REVIEWS ===")
    val dfReview = spark.read.format("jdbc")
      .option("url", pgUrl)
      .option("dbtable", "yelp.review")
      .option("user", "tpid").option("password", "tpid")
      .load()

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
    .join(dimCommerce, Seq("COMMERCE_ID"), "inner")
    .join(dimUser, Seq("USER_ID"), "inner")
    .dropDuplicates("REVIEW_ID")
    faitReviews.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_REVIEWS", oracleProps)
    println("   -> DONE")

    spark.stop()
  }
}