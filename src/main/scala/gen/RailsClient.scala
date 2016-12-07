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
import play.api.libs.ws.{WSRequest, WSResponse}

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

  private val host = "https://tv-shows-calendar-app.herokuapp.com"
  private val key = "zQSEZwSwVPao8hpZoX381NZGX".reverse
  private val wsClient = NingWSClient()
  private val throttler = new HttpThrottler(Rate(100, 1.second))
  private case object Conflict

  def addOrUpdateSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    addSerie(serie).flatMap {
      case None => Future.successful(())
      case Some(Conflict) => updateSerie(serie)
    }
  }

  private def addSerie(serie: Serie)(implicit e: ExecutionContext): Future[Option[Conflict.type]] = {
    callForMaybeConflict(wsClient
      .url(s"$host/shows")
      .withBody(Json.toJson(serie))
      .withMethod("POST")
    )
  }

  private def updateSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    callForUnit(wsClient
      .url(s"$host/shows/${serie.id}")
      .withBody(Json.toJson(serie))
      .withMethod("PUT")
    )
  }

  def addOrUpdateSeason(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Unit] = {
    addSeason(serie, season).flatMap {
      case None => Future.successful(())
      case Some(Conflict) => updateSeason(serie, season)
    }
  }

  private def addSeason(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Option[Conflict.type]] = {
    callForMaybeConflict(wsClient
      .url(s"$host/shows/${serie.id}/seasons")
      .withBody(Json.toJson(season))
      .withMethod("POST")
    )
  }

  private def updateSeason(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Unit] = {
    callForUnit(wsClient
      .url(s"$host/shows/${serie.id}/seasons/${season.number}")
      .withBody(Json.toJson(season))
      .withMethod("PUT")
    )
  }

  def deleteSeasonsOfSerie(serie: Serie)(implicit e: ExecutionContext): Future[Unit] = {
    getSeasonsOfSerie(serie).flatMap { seasons =>
      Future.traverse(seasons){ season =>
        deleteSeasonOfSerie(serie, season)
      }.map(_ => ())
    }
  }

  private def getSeasonsOfSerie(serie: Serie)(implicit e: ExecutionContext): Future[Seq[Season]] = {
    callForRawResponse(wsClient
      .url(s"$host/shows/${serie.id}/seasons")
    ).map(_.json.as[Seq[Season]])
  }

  private def deleteSeasonOfSerie(serie: Serie, season: Season)(implicit e: ExecutionContext): Future[Unit] = {
    callForUnit(wsClient
      .url(s"$host/shows/${serie.id}/seasons/${season.number}")
      .withMethod("DELETE")
    )
  }

  private def callForMaybeConflict(req: WSRequest)(implicit e: ExecutionContext): Future[Option[Conflict.type]] = {
    call(req).map( _.left.toOption)
  }

  private def callForRawResponse(req: WSRequest)(implicit e: ExecutionContext): Future[WSResponse] = {
    call(req)
      .map {
      case Left(Conflict) => err(s"Got a 409 on ${req.method} ${req.url}, that should not be possible")
      case Right(response) => response
    }
  }

  private def callForUnit(req: WSRequest)(implicit e: ExecutionContext): Future[Unit] =
    callForRawResponse(req).map(_ => ())

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
