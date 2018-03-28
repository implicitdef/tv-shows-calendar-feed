package feed.db

import com.github.mauricio.async.db.SSLConfiguration.Mode
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.postgresql.util.URLParser
import com.github.mauricio.async.db.{Configuration, SSLConfiguration}
import feed.Domain.{Season, Serie}
import feed.utils.Pimp._
import play.api.libs.json.{JsValue, Json}
import feed.db.NeoDbAccessor.Target
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

object NeoDbAccessor {
  sealed trait Target
  object Target {
    case object Local extends Target
    case object ReworkNode extends Target
  }
}

class NeoDbAccessor {

  private def configuration(target: Target): Configuration = target match {
    case Target.Local =>
      Configuration(
        username = "manu",
        host = "localhost",
        port = 5432,
        password = None,
        database = Some("tv_shows_calendar")
      )
    case Target.ReworkNode =>
      // This params may rotate periodically, check heroku to get the latest one
      Configuration(
        username = "fvivwakvmsirvy".reverse,
        host = "moc.swanozama.etupmoc.1-tsew-ue.46-622-57-45-2ce".reverse,
        port = 5432,
        password = Some("be117185edaf16b6bf7c18c9d7c7db3b4457987e507d7cb47f3e58c9c4efae84".reverse),
        database = Some("265aagb23odbed".reverse),
        ssl = SSLConfiguration(mode = Mode.Require)
      )
  }

  private def futureConnection(target: Target) =
    new PostgreSQLConnection(configuration(target)).connect

  def deleteAllJsonData(target: Target): Future[Unit] = {
    for {
      c <- futureConnection(target)
      _ <- c.sendQuery("TRUNCATE raw_json_data")
    } yield ()
  }

  def insertJson(target: Target, jsValue: JsValue): Future[Unit] =
    for {
      c <- futureConnection(target)
      _ <- c.sendPreparedStatement(
        "INSERT INTO raw_json_data (content) " +
          "VALUES (?)",
        values = Seq(Json.stringify(jsValue))
      )
    } yield ()

}
