import org.apache.spark.sql.{SparkSession, SaveMode}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.jdbc.JdbcDialects

object Main {
  def main(args: Array[String]): Unit = {
    val spark = SparkSession.builder().appName("ETL").master("local[*]").getOrCreate()
    import spark.implicits._
    JdbcDialects.registerDialect(new OracleDialect)

    // credentials lus depuis l'environnement (cf .env a la racine, gitignore)
    // si la variable n'existe pas -> ca plante direct, c'est voulu
    val oracleUser = sys.env("ORACLE_USER")
    val oraclePassword = sys.env("ORACLE_PASSWORD")

    val oracleUrl = "jdbc:oracle:thin:@//stendhal.iem:1521/enss2025"
    val oracleProps = new java.util.Properties()
    oracleProps.setProperty("user", oracleUser)
    oracleProps.setProperty("password", oraclePassword)
    oracleProps.setProperty("driver", "oracle.jdbc.OracleDriver")

    println("=== [1/8] dim_temps ===")

    // calendrier complet 2004-2025, un enregistrement par jour
    val dimTemps = spark.sql("SELECT explode(sequence(to_date('2004-01-01'), to_date('2025-12-31'), interval 1 day)) as d")
      .select(
        date_format($"d", "yyyyMMdd").cast("int").as("DATE_ID"),
        $"d".as("DATE_COMPLETE"),
        year($"d").as("ANNEE"),
        quarter($"d").as("TRIMESTRE"),
        month($"d").as("MOIS"),
        date_format($"d", "MMMM").as("NOM_MOIS"),
        dayofmonth($"d").as("JOUR"),
        date_format($"d", "EEEE").as("JOUR_SEMAINE"),
        when(date_format($"d", "E").isin("Sat", "Sun"), 1).otherwise(0).as("EST_WEEKEND")
      ).distinct()

    dimTemps.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_TEMPS", oracleProps)
    println(s"   -> DONE: ${dimTemps.count()} lignes")

    spark.stop()
  }
}