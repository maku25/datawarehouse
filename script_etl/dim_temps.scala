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

    println("=== [1/8] DIM_TEMPS ===")
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