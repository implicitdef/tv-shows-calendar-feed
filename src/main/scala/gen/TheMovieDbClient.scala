package gen

import java.time.LocalDate
import java.time.format.DateTimeParseException

import gen.Domain.{Serie, TimeRange}
import gen.TheMovieDbClient.SeasonEndpoint._
import gen.TheMovieDbClient.{DiscoverEndpoint, SeasonEndpoint, TvShowEndpoint}
import gen.utils.Collector
import gen.utils.Pimp._
import odelay.jdk
import play.api.libs.json._
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.Exception._
object TheMovieDbClient {

  object DiscoverEndpoint {
    case class Result(
                       results: Seq[TvShow]
                     )
    case class TvShow(
                       id: Int,
                       name: String
                     )
    implicit val reads2 = Json.format[TvShow]
    implicit val reads3 = Reads.seq[TvShow]
    implicit val reads1 = Json.format[Result]
  }

  object TvShowEndpoint {
    case class TvShow(
                       seasons: Seq[Season]
                     )
    case class Season(
                       season_number: Int
                     )
    implicit val reads2 = Json.format[Season]
    implicit val reads3 = Reads.seq[Season]
    implicit val reads1 = Json.format[TvShow]
  }

  object SeasonEndpoint {
    case class Season(
                       episodes: Seq[Episode]
                     )
    case class Episode(
                        air_date: Option[String]
                      )
    implicit val reads2 = Json.format[Episode]
    implicit val reads3 = Reads.seq[Episode]
    implicit val reads1 = Json.format[Season]
  }

}

class TheMovieDbClient {

  implicit val localDateOrdering: Ordering[LocalDate] = Ordering.fromLessThan(_ isBefore _)

  private val apiKey = "000ffc8b6e767158ff5489a8daba11c2"
  private val baseUrl = "https://api.themoviedb.org/3"

  val wsClient = NingWSClient()
  val httpThrottler = new HttpThrottler


  def getBestSeriesAtPage(page: Int = 1)(implicit e: ExecutionContext, c: Collector): Future[Seq[Serie]] =
    callWithBackoff(s"$baseUrl/discover/tv", "sort_by" -> "popularity.desc", "page" -> page.toString).map {
      _
        .as[DiscoverEndpoint.Result]
        .results.map(tvShow => Serie(tvShow.id, tvShow.name))
    }

  def getSeasonsNumbers(serie: Serie)(implicit e: ExecutionContext, c: Collector): Future[Seq[Int]] =
    callWithBackoff(s"$baseUrl/tv/${serie.id}").map {
      _
        .asOpt[TvShowEndpoint.TvShow]
        .map {
          _.seasons.map(_.season_number)
            // sometimes there's a weird season 0
            .filterNot(_ == 0)
        }.getOrElse {
          c.push(s"Failed to parse the sesons numbers of tvShow ${serie.name}")
          Nil
        }

    }

  def getSeasonsTimeRange(serie: Serie, season: Int)(implicit e: ExecutionContext, c: Collector): Future[Option[TimeRange]] =
    callWithBackoff(s"$baseUrl/tv/${serie.id}/season/$season").map {
      _
        .as[SeasonEndpoint.Season]
        .episodes.map(_.air_date)
        .flatMap { maybeAirDate =>
          if (maybeAirDate.isEmpty)
            c.push(s"No air_date for an episode of ${serie.name} season $season")
          maybeAirDate
        }
        .flatMap(d => swallowing(classOf[DateTimeParseException])(LocalDate.parse(d)))
        .sorted
      match {
        case Seq() =>
          c.push(s"${serie.name} season $season has zero episodes")
          None
        case seq =>
          Some(TimeRange(seq.head, seq.last))
      }
    }

  def shutdown() = {
    httpThrottler.shutdown()
    wsClient.close()
  }

  val timer = new jdk.JdkTimer(poolSize = 100)
  private def callWithBackoff(theUrl: String, extraParams: (String, String)*)(implicit e: ExecutionContext, c: Collector): Future[JsValue] = {
    // there's an implicit Success for Either, which will make it retry if it's
    // a Left, i.e. if we received a 429
    retry.Backoff(max = 50)(timer).apply(() =>
      call(theUrl, extraParams:_*)
    ).map(_.right.getOrElse(err(s"The exponential backoff failed to prevent the 429")))
  }

  object Received429

  private def call(theUrl: String, extraParams: (String, String)*)
                  (implicit e: ExecutionContext, c: Collector): Future[Either[Received429.type, JsValue]] = {
    val req = wsClient
      .url(theUrl)
      .withQueryString("api_key" -> apiKey)
      .withQueryString(extraParams:_*)
    httpThrottler.call(req).map { wsResponse =>
      if ((200 to 299).contains(wsResponse.status))
        Right(Json.parse(wsResponse.body))
      else if (wsResponse.status == 429)
        Left(Received429)
      else
        sys.error(s"Unexpected status code ${wsResponse.status} : ${wsResponse.body}")
    }
  }


}
