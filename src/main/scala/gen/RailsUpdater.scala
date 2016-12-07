package gen
import gen.Domain.SerieWithSeasons
import gen.Injection._
import gen.utils.Pimp._

import scala.concurrent.{ExecutionContext, Future}
class RailsUpdater {

  def updateRails(series: Seq[SerieWithSeasons])(implicit e: ExecutionContext): Future[Unit] = {
    logger(this).info("Deleting all seasons")
    for {
      _ <- dbAccessor.deleteAllSeasons
      logger(this).info("Deleting all series")
      _ <- dbAccessor.deleteAllSeries
      _ <- series.foldLeft(fuccess){ case (previous, serie) =>
        previous.flatMap {
          _ => insertSerieThenSeasons(serie)
        }
      }
    } yield ()
  }

  def insertSerieThenSeasons(serie: SerieWithSeasons): Future[Unit] = {
    logger(this).info(s"Inserting serie ${serie.serie.id}")
    dbAccessor.insertSerie(serie.serie).flatMap { _ =>
      serie.seasons.foldLeft(fuccess){ case (previous, season) =>
        previous.flatMap { _ =>
          logger(this).info(s"Inserting season ${season.number} for serie ${serie.serie.id}")
          dbAccessor.insertSeason(serie.serie, season)
        }
      }
    }
  }
}
