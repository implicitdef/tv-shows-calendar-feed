package feed
import feed.Domain.SerieWithSeasons
import feed.Injection._
import feed.utils.Pimp._

import scala.concurrent.{ExecutionContext, Future}
class RailsUpdater {

  def updateRails(series: Seq[SerieWithSeasons])(implicit e: ExecutionContext): Future[Unit] = {
    logger(this).info("Deleting all seasons")
    for {
      _ <- classicDbAccessor.deleteAllSeasons
      _ = logger(this).info("Deleting all series")
      _ <- classicDbAccessor.deleteAllSeries
      _ <- bulkInsertSeries(series)
      _ <- bulkInsertSeasons(series)
    } yield ()
  }

  def bulkInsertSeries(series: Seq[SerieWithSeasons]): Future[Unit] =
    series.grouped(50).zipWithIndex.foldLeft(fuccess){ case (prev, (seriesGroup, idx)) =>
      prev.flatMap { _ =>
        logger(this).info(s"Inserting ${series.size} series ($idx)")
        classicDbAccessor.insertSeries(seriesGroup.map(_.serie))
      }
    }

  def bulkInsertSeasons(series: Seq[SerieWithSeasons]): Future[Unit] =
    series.flatMap { serie =>
      serie.seasons.map { season =>
        (serie.serie, season)
      }
    }.grouped(50).zipWithIndex.foldLeft(fuccess){ case (prev, (seasonsGroup, idx)) =>
      prev.flatMap { _ =>
        logger(this).info(s"Inserting ${seasonsGroup.size} seasons ($idx)")
        classicDbAccessor.insertSeasons(seasonsGroup)
      }
    }


}
