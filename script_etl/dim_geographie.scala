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

    println("=== [2/8] DIM_GEOGRAPHIE ===")
    val rawBusiness = spark.read.json("/data/M2BDIA-ID-Ressources/dataset/yelp_academic_dataset_business.json")
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
    println(s"   -> DONE: ${dimGeo.count()} lignes")

    spark.stop()
  }
}