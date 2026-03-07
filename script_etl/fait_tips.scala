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

    println("=== [8/8] FAIT_TIPS ===")
    val faitTips = spark.read.option("header", "true")
      .csv("/data/M2BDIA-ID-Ressources/dataset/yelp_academic_dataset_tip.csv")
      .select(
        $"business_id".as("COMMERCE_ID"),
        $"user_id".as("USER_ID"),
        date_format(to_date($"date"), "yyyyMMdd").cast("int").as("DATE_ID"),
        $"compliment_count".cast("int").as("NB_COMPLIMENTS")
      )
      .join(dimCommerce, Seq("COMMERCE_ID"), "inner")
      .join(dimUser, Seq("USER_ID"), "inner")
      .groupBy("COMMERCE_ID", "USER_ID", "DATE_ID")
      .agg(sum("NB_COMPLIMENTS").as("NB_COMPLIMENTS"))
    faitTips.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_TIPS", oracleProps)
    println("   -> DONE")

    spark.stop()
  }
}