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

    println("=== [5/8] DIM_UTILISATEUR ===")
    val dfUserBase = spark.read.format("jdbc")
      .option("url", pgUrl)
      .option("dbtable", "yelp.\"user\"")
      .option("user", "tpid").option("password", "tpid")
      .load()

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
      ).dropDuplicates("USER_ID")
    dimUser.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_UTILISATEUR", oracleProps)
    println(s"   -> DONE: ${dimUser.count()} lignes")

    spark.stop()
  }
}