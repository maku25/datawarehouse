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

    println("=== [5/8] dim_utilisateur ===")

    // table user de base (attention aux quotes, "user" est un mot reserve en sql)
    val dfUserBase = spark.read.format("jdbc")
      .option("url", pgUrl)
      .option("dbtable", "yelp.\"user\"")
      .option("user", pgUser).option("password", pgPassword)
      .load()

    // statut elite : c'est une table separee avec une ligne par annee d'elite
    // on agrege pour compter combien d'annees chaque user a ete elite
    val dfEliteAgg = spark.read.format("jdbc")
      .option("url", pgUrl).option("dbtable", "yelp.elite")
      .option("user", pgUser).option("password", pgPassword).load()
      .groupBy("user_id").agg(count("*").as("NB_ANNEES_ELITE"))

    // left join : on garde tous les users meme s'ils n'ont jamais ete elite
    val dimUser = dfUserBase.join(dfEliteAgg, Seq("user_id"), "left")
      .select(
        $"user_id".as("USER_ID"),
        $"name".as("NOM"),
        to_date($"yelping_since").as("DATE_INSCRIPTION"),
        $"review_count".as("NB_AVIS_TOTAL"),
        $"fans".as("NB_FANS"),
        $"average_stars".as("NOTE_MOY_DONNEE"),
        // flag booleen calcule a partir de la presence dans la table elite
        when($"NB_ANNEES_ELITE".isNotNull, 1).otherwise(0).as("EST_ELITE"),
        // si jamais elite -> 0 plutot que null pour eviter les surprises en aval
        coalesce($"NB_ANNEES_ELITE", lit(0)).as("NB_ANNEES_ELITE")
      ).dropDuplicates("USER_ID")

    dimUser.write.mode(SaveMode.Append).jdbc(oracleUrl, "DIM_UTILISATEUR", oracleProps)
    println(s"   -> DONE: ${dimUser.count()} lignes")

    spark.stop()
  }
}