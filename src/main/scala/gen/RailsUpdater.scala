package gen

import gen.Domain.SerieWithSeasons

import scala.concurrent.{ExecutionContext, Future}
import Injection._
class RailsUpdater {


  def updateRails(series: Seq[SerieWithSeasons])(implicit e: ExecutionContext): Future[Unit] = {
    Future.traverse(series){ serieWithSeason =>
      val serie = serieWithSeason.serie
      for {
        _ <- railsClient.addOrUpdateSerie(serie)
        _ <- railsClient.deleteSeasonsOfSerie(serie)
        _ <- Future.traverse(serieWithSeason.seasons) { season =>
          railsClient.addOrUpdateSeason(serie, season)
        }
      } yield ()
    }.map(_ => ())
  }


}
