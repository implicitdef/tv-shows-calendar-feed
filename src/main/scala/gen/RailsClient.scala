package gen

import java.time.LocalDate

import akka.contrib.throttle.Throttler.Rate
import gen.Domain.{Season, Serie, TimeRange}
import gen.RailsClient._
import gen.utils.Pimp._
import odelay.jdk
import play.api.libs.json.{Json, _}
import play.api.libs.ws.ning.NingWSClient
import play.api.libs.ws.{WSRequest, WSResponse}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

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

  case object Conflict

}

class RailsClient {

  private val host = "https://tv-shows-calendar-app.herokuapp.com"
  private val key = "zQSEZwSwVPao8hpZoX381NZGX".reverse
  private val wsClient = NingWSClient()
  private val throttler = new HttpThrottler(Rate(50, 1.second))

  def addSerie(serie: Serie)(implicit e: ExecutionContext): Future[Option[Conflict.type]] = {
    callForMaybeConflict(wsClient
      .url(s"$host/shows")
      .withBody(Json.toJson(serie))
      .withMethod("POST")
    )
  }

  def updateSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    callForUnit(wsClient
      .url(s"$host/shows/${serie.id}")
      .withBody(Json.toJson(serie))
      .withMethod("PUT")
    )
  }


  def addSeason(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Option[Conflict.type]] = {
    callForMaybeConflict(wsClient
      .url(s"$host/shows/${serie.id}/seasons")
      .withBody(Json.toJson(season))
      .withMethod("POST")
    )
  }

  def updateSeason(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Unit] = {
    callForUnit(wsClient
      .url(s"$host/shows/${serie.id}/seasons/${season.number}")
      .withBody(Json.toJson(season))
      .withMethod("PUT")
    )
  }


  def getSeasonsOfSerie(serie: Serie)(implicit e: ExecutionContext): Future[Seq[Season]] = {
    callForRawResponse(wsClient
      .url(s"$host/shows/${serie.id}/seasons")
    ).map(_.json.as[Seq[Season]])
  }

  def deleteSeasonOfSerie(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Unit] = {
    callForUnit(wsClient
      .url(s"$host/shows/${serie.id}/seasons/${season.number}")
      .withMethod("DELETE")
    )
  }

  private def callForUnit(req: WSRequest)(implicit e: ExecutionContext): Future[Unit] =
    callForRawResponse(req).map(_ => ())

  private def callForMaybeConflict(req: WSRequest)(implicit e: ExecutionContext): Future[Option[Conflict.type]] = {
    callWithBackoff(req).map( _.left.toOption)
  }

  private def callForRawResponse(req: WSRequest)(implicit e: ExecutionContext): Future[WSResponse] = {
    callWithBackoff(req)
      .map {
      case Left(Conflict) => err(s"Got a 409 on ${req.method} ${req.url}, that should not be possible")
      case Right(response) => response
    }
  }

  private val timer = new jdk.JdkTimer(poolSize = 100)
  private def callWithBackoff(req: WSRequest)(implicit e: ExecutionContext): Future[Either[Conflict.type, WSResponse]] = {
    // this will make retry when we receive a 503
    implicit val isSuccess = retry.Success[Either[Conflict.type, WSResponse]]{
      case Left(_) => true
      case Right(res) => res.status != 503
    }
    retry.Backoff(max = 50)(timer).apply(() =>
      call(req)
    )(isSuccess, e).map {
      case Right(res) if res.status == 503 =>
        err("Failed to prevent the 503 despite the retry")
      case other => other
    }
  }

  private def call(req: WSRequest)(implicit e: ExecutionContext): Future[Either[Conflict.type, WSResponse]] = {
    logger(this).info(s">> ${req.method} ${req.url}")
    throttler.call(
      req
        .withQueryString("key" -> key)
    ).map { res =>
      val status = res.status
      if (status == 409) Left(Conflict)
      else if (!(200 to 299).contains(status)) {
        err(s"Failed request to ${req.url}, got $status : ${res.body}")
      } else Right(res)
    }
  }


  def shutdown() = {
    throttler.shutdown()
    wsClient.close()
  }

}
