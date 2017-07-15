package feed.db

import com.github.mauricio.async.db.SSLConfiguration.Mode
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.{Configuration, SSLConfiguration}
import feed.Domain.{Season, Serie}
import feed.utils.Pimp._
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class NeoDbAccessor {

  private val configuration =
    Configuration(
      username = "manu",
      host = "localhost",
      port = 5432,
      password = None,
      database = Some("tv_shows_calendar")//,
      //ssl = SSLConfiguration(mode = Mode.Require)
    )

  private lazy val futureConnection = new PostgreSQLConnection(configuration).connect

  def deleteAllJsonData: Future[Unit] = {
    for {
      c <- futureConnection
      _ <- c.sendQuery("TRUNCATE raw_json_data")
    } yield ()
  }

  def insertJson(jsValue: JsValue): Future[Unit] =
    for {
      c <- futureConnection
      _ <- c.sendPreparedStatement(
        "INSERT INTO raw_json_data (content) " +
          "VALUES (?)",
        values = Seq(Json.stringify(jsValue))
      )
    } yield ()

}
