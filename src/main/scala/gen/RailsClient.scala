package gen

import java.time.LocalDate

import gen.Domain.{Season, Serie, TimeRange}
import gen.utils.Pimp._
import play.api.libs.json.{Json, _}
import play.api.libs.ws.ning.NingWSClient

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try
import RailsClient._
import akka.contrib.throttle.Throttler.Rate
import scala.concurrent.duration._

object RailsClient {


  implicit val localDateReads = new Reads[LocalDate]{
    override def reads(json: JsValue): JsResult[LocalDate] =
      json.validate[String].flatMap { str =>
        Try(LocalDate.parse(str)).toOption
          .map(d => JsSuccess(d))
          .getOrElse(JsError(s"$str is not a date"))
      }
  }

  implicit val serieFormat = Domain.serieformat
  implicit val seasonWrites = new Writes[Season] {
    override def writes(s: Season): JsValue = Json.obj(
      "number" -> s.number,
      "start_date" -> s.time.start.toString,
      "end_date" -> s.time.end.toString
    )
  }
  implicit val seasonReads = new Reads[Season] {
    override def reads(json: JsValue): JsResult[Season] =
      json.validate[JsObject].flatMap { obj =>
        for {
          number <- (json \ "number").validate[Int]
          start <- (json \ "start_date").validate[LocalDate]
          end <- (json \ "end_date").validate[LocalDate]
        } yield {
          val range = TimeRange(start = start, end = end)
          Season(number, range)
        }
      }
  }
}

class RailsClient {

  val host = "https://tv-shows-calendar-app.herokuapp.com"

  val wsClient = NingWSClient()

  val throttler = new HttpThrottler(Rate(100, 1.second))


  def addOrUpdateSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    val url = s"$host/shows"
    logger(this).info(s">> POST $url")
    throttler.call(wsClient
      .url(url)
      .withBody(Json.toJson(serie))
      .withMethod("POST")
    )
      .flatMap { r =>
        if (r.status == 409) {
          val url2 = s"$host/shows/${serie.id}"
          logger(this).info(s">> PUT $url2")
          throttler.call(wsClient
            .url(url2)
            .withBody(Json.toJson(serie))
            .withMethod("PUT")
          )
            .map { r2 =>
              if (! (200 to 299).contains(r2.status))
                err(s"Failed request to $url2, got ${r2.status} : ${r2.body}")
              else ()
            }
        } else Future.successful(())
      }
  }

  def addOrUpdateSeason(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Unit] = {
    val url = s"$host/shows/${serie.id}/seasons"
    logger(this).info(s">> POST $url")
    throttler.call(wsClient
      .url(url)
      .withBody(Json.toJson(season))
      .withMethod("POST")
    )
      .flatMap { r =>
        if (r.status == 409) {
          val url2 = s"$host/shows/${serie.id}/seasons/${season.number}"
          logger(this).info(s">> PUT $url2")
          throttler.call(wsClient
            .url(url2)
            .withBody(Json.toJson(season))
            .withMethod("PUT")
          )
            .map { r2 =>
              if (! (200 to 299).contains(r2.status))
                err(s"Failed request to $url2, got ${r2.status} : ${r2.body}")
              else ()
            }
        } else if (! (200 to 299).contains(r.status)) {
          err(s"Failed request to $url, got ${r.status} : ${r.body}")
        } else Future.successful(())
      }
  }

  def deleteSeasonsOfSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    val url = s"$host/shows/${serie.id}/seasons"
    logger(this).info(s">> GET $url")
    throttler.call(
      wsClient
      .url(url)
    )
      .flatMap { r =>
        if (! (200 to 299).contains(r.status)) {
          err(s"Failed request to $url, got ${r.status} : ${r.body}")
        } else {
          val seasons = r.json.as[Seq[Season]]
          Future.traverse(seasons){ season =>
            val url2 = s"$host/shows/${serie.id}/seasons/${season.number}"
            logger(this).info(s">> DELETE $url")
            throttler.call(wsClient
              .url(url2)
              .withMethod("DELETE")
            )
              .map { r2 =>
                if (! (200 to 299).contains(r2.status)) {
                  err(s"Failed request to $url, got ${r.status} : ${r.body}")
                }
              }
          }
        }.map(_ => ())
      }
  }

}
