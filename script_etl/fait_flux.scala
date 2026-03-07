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

    println("Lecture DIM_COMMERCE depuis Oracle...")
    val dimCommerce = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_COMMERCE")
      .option("user", login).option("password", login)
      .option("driver", "oracle.jdbc.OracleDriver").load()
      .select("COMMERCE_ID").cache()
    println(s"   -> ${dimCommerce.count()} commerces")

    println("=== [7/8] FAIT_FLUX ===")
    val faitFlux = spark.read.json("/data/M2BDIA-ID-Ressources/dataset/yelp_academic_dataset_checkin.json")
      .withColumn("date_str", explode(split($"date", ",")))
      .select(
        $"business_id".as("COMMERCE_ID"),
        date_format(trim($"date_str"), "yyyyMMdd").cast("int").as("DATE_ID")
      )
      .join(dimCommerce, Seq("COMMERCE_ID"), "inner")
      .groupBy("COMMERCE_ID", "DATE_ID")
      .agg(count("*").as("NB_CHECKINS"))
    faitFlux.write.mode(SaveMode.Append).jdbc(oracleUrl, "FAIT_FLUX", oracleProps)
    println("   -> DONE")

    spark.stop()
  }
}