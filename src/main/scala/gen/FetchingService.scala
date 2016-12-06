package gen

import gen.Domain.{Season, SeasonWithSerie, Serie, SerieWithSeasons}
import gen.utils.Collector
import gen.utils.Pimp._

import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

class FetchingService(client: TheMovieDbClient) {

  def fetch(pagesToFetch: Int = 100)
           (implicit e: ExecutionContext,
            c: Collector): Future[Seq[SerieWithSeasons]] =
    timed {
      (1 to pagesToFetch)
        .map(fetchPage)
        .sequence
        .map(_.flatten)
    }
      .map(logDuration)
      .tap(logResult)

  private def fetchPage(pageNumber: Int)
                       (implicit e: ExecutionContext,
                        c: Collector): Future[Seq[SerieWithSeasons]] =
    client.getBestSeriesAtPage(pageNumber).flatMap { series =>
      series.map(fetchForSerie).sequence
    }

  private def fetchForSerie(serie: Serie)
                           (implicit e: ExecutionContext,
                            c: Collector): Future[SerieWithSeasons] =
    client.getSeasonsNumbers(serie).flatMap { seasonsNumber =>
      seasonsNumber.map { seasonNumber =>
        client.getSeasonsTimeRange(serie, seasonNumber).map {
          _.map { time =>
            Season(seasonNumber, time)
          }
        }
      }.sequence
       .map(_.flatten)
       .map(seasons => SerieWithSeasons(serie, seasons))
    }

  private def extractSeries(seasonWithSeries: Seq[SeasonWithSerie]): Seq[Serie] =
    seasonWithSeries.map(_.serie).distinct

  private def logDuration[A](res: (A, Duration)): A =
    res match { case (r, duration) =>
      logger(this).info(s"Queried in ${duration.toMinutes}mins (= ${duration.toSeconds}s)")
      r
    }

  private def logResult(res: Seq[SerieWithSeasons]) =
    logger(this).info(s"Loaded ${res.size} series, ${res.map(_.seasons.size).sum} total")



}
