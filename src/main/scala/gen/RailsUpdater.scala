package gen
import utils.Pimp._
import gen.Domain.{Season, Serie, SerieWithSeasons}

import scala.concurrent.{ExecutionContext, Future}
import Injection._
import gen.RailsClient.Conflict
class RailsUpdater {

  def updateRails(series: Seq[SerieWithSeasons])(implicit e: ExecutionContext): Future[Unit] = {
    series.foldLeft(fuccess){ case (previous, serie) =>
      previous.flatMap(_ => processSerie(serie))
    }
  }

  private def processSerie(serie: SerieWithSeasons)(implicit e: ExecutionContext): Future[Unit] = {
    addOrUpdateSerie(serie.serie).flatMap { _ =>
      deleteSeasons(serie.serie).flatMap { _ =>
        addSeasons(serie.serie, serie.seasons)
      }
    }
  }


  private def addOrUpdateSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    railsClient.addSerie(serie).flatMap {
      case Some(Conflict) => railsClient.updateSerie(serie)
      case None => fuccess
    }
  }

  private def deleteSeasons(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    railsClient.getSeasonsOfSerie(serie).flatMap { seasons =>
      seasons.foldLeft(fuccess){ case (previous, season) =>
        previous.flatMap { _ =>
          railsClient.deleteSeasonOfSerie(serie, season)
        }
      }
    }
  }

  private def addSeasons(serie: Serie, seasons: Seq[Season])(implicit e: ExecutionContext): Future[Unit] = {
    seasons.foldLeft(fuccess){ case (previous, season) =>
      previous.flatMap { _ =>
        railsClient.addSeason(serie, season).map {
          case Some(Conflict) => err("Got a conflict when adding a season, but that should not happened since we deleted them all")
          case _ => ()
        }
      }
    }
  }

}
