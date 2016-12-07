package gen.db

import com.github.mauricio.async.db.SSLConfiguration.Mode
import com.github.mauricio.async.db.postgresql.PostgreSQLConnection
import com.github.mauricio.async.db.{Configuration, SSLConfiguration}

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DbAccessor {

  private val configuration =
    Configuration(
      username = "bihujifleyfrzu".reverse,
      host = "moc.swanozama.etupmoc.1-tsew-ue.201-59-742-45-2ce".reverse,
      port = 5432,
      password = Some("9nT3aIw5Dtj4cf-bHuuYJM1kPZ".reverse),
      database = Some("mmj9mevmuniocd".reverse),
      ssl = SSLConfiguration(mode = Mode.Require)
    )
  private lazy val futureConnection = new PostgreSQLConnection(configuration).connect


  def deleteAllShows = ???
  def deleteAllSeasons = ???

  def insertShows = ???
  def insertSeasons = ???

  def testQuery: Future[Int] =
    for {
      c <- futureConnection
      queryResult <- c.sendQuery("SELECT 5")
    } yield {
      queryResult.rows match {
        case Some(resultSet) => {
          val row = resultSet.head
          row(0).asInstanceOf[Int]
        }
        case None => -1
      }
    }

}
