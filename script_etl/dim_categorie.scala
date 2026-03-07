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

    // Lire DIM_COMMERCE depuis Oracle
    println("Lecture DIM_COMMERCE depuis Oracle...")
    val dimCommerce = spark.read.format("jdbc")
      .option("url", oracleUrl).option("dbtable", "DIM_COMMERCE")
      .option("user", login).option("password", login)
      .option("driver", "oracle.jdbc.OracleDriver").load()
      .select("COMMERCE_ID", "CATEGORIES").cache()
    println(s"   -> ${dimCommerce.count()} commerces")

    println("=== [4/8] DIM_CATEGORIE + PONT ===")
    val categoriesExploded = dimCommerce
      .filter($"CATEGORIES".isNotNull)
      .select(
        $"COMMERCE_ID",
        explode(split($"CATEGORIES", ",\\s*")).as("NOM_CATEGORIE")
      )
      .filter($"NOM_CATEGORIE" =!= "" && $"NOM_CATEGORIE".isNotNull)
      .withColumn("NOM_CATEGORIE", trim($"NOM_CATEGORIE"))

    val dimCategorie = categoriesExploded
      .select("NOM_CATEGORIE")
      .distinct()
      .withColumn("CATEGORIE_ID", monotonically_increasing_id())
      .cache()
    dimCategorie.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_CATEGORIE", oracleProps)
    println(s"   -> ${dimCategorie.count()} categories")

    val pontCommCat = categoriesExploded
      .join(dimCategorie, Seq("NOM_CATEGORIE"), "inner")
      .select("COMMERCE_ID", "CATEGORIE_ID")
      .distinct()
    pontCommCat.write.mode(SaveMode.Append).jdbc(oracleUrl, "PONT_COMMERCE_CATEGORIE", oracleProps)
    println(s"   -> ${pontCommCat.count()} associations")

    spark.stop()
  }
}