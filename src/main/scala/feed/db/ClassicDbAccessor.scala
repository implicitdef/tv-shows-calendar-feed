package feed.db

import com.github.mauricio.async.db.SSLConfiguration.Mode
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.{Configuration, SSLConfiguration}
import feed.Domain.{Season, Serie}
import feed.utils.Pimp._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._

class ClassicDbAccessor {

  private val configuration =
    Configuration(
      username = "iocjacmkxobsia".reverse,
      host = "moc.swanozama.etupmoc.1-tsew-ue.091-932-57-45-2ce".reverse,
      port = 5432,
      password = Some("a8e6e398bebe3e9d6c9c43c8796afb9146d9a1d721f914a393c9e317b19f5e7e".reverse),
      database = Some("1i9f80ge9ce31d".reverse),
      ssl = SSLConfiguration(mode = Mode.Require)
    )

  private lazy val futureConnection = new PostgreSQLConnection(configuration).connect

  def deleteAllSeries: Future[Unit] =
    for {
      c <- futureConnection
      _ <- c.sendQuery("TRUNCATE shows")
    } yield ()

  def deleteAllSeasons: Future[Unit] =
    for {
      c <- futureConnection
      _ <- c.sendQuery("TRUNCATE seasons")
    } yield ()

  def insertSerie(serie: Serie): Future[Unit] =
    for {
      c <- futureConnection
      _ <- c.sendPreparedStatement(
        "INSERT INTO shows (id, name, created_at, updated_at) " +
        "VALUES (?, ?, NOW(), NOW())",
        values = Seq(serie.id, serie.name)
      )
    } yield ()


  def insertSeries(series: Seq[Serie]): Future[Unit] =
    for {
      c <- futureConnection
      _ <- c.sendPreparedStatement(
        "INSERT INTO shows (id, name, created_at, updated_at) " +
        "VALUES " +
        series.map(_ => "(?, ?, NOW(), NOW())").mkString(", "),
        values = series.flatMap(serie => Seq(serie.id, serie.name))
      )
    } yield ()

  def insertSeason(serie: Serie, season: Season) =
    for {
      c <- futureConnection
      _ <- c.sendPreparedStatement(
        "INSERT INTO seasons (show_id, number, start_date, end_date, created_at, updated_at) " +
          "VALUES (?, ?, ?, ?, NOW(), NOW())",
        values = Seq(serie.id, season.number, season.time.start, season.time.end)
      )
    } yield ()

  def insertSeasons(seq: Seq[(Serie, Season)]) =
    if (seq.isEmpty) fuccess
    else {
      for {
        c <- futureConnection
        _ <- c.sendPreparedStatement(
          "INSERT INTO seasons (show_id, number, start_date, end_date, created_at, updated_at) " +
           "VALUES " +
            seq.map( _ => "(?, ?, ?, ?, NOW(), NOW())").mkString(", "),
           values = seq.flatMap { case (serie, season) =>
             Seq(serie.id, season.number, season.time.start, season.time.end)
           }
        )
      } yield ()
    }


}
