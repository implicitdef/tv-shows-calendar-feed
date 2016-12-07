package feed

import java.time.{LocalDate, LocalDateTime}

import play.api.libs.functional.syntax._
import play.api.libs.json._


object Domain {

  case class Serie(
    id: Int,
    name: String
  )
  case class Season(
    number: Int,
    time: TimeRange
  )
  case class TimeRange(
    start: LocalDate,
    end: LocalDate
  )
  case class SeasonWithSerie(
    serie: Serie,
    number: Int,
    time: TimeRange
  )
  case class SerieWithSeasons(
    serie: Serie,
    seasons: Seq[Season]
  )

  implicit val trWrites = new Writes[TimeRange] {
    override def writes(s: TimeRange) = Json.obj(
      "start" -> s.start.atStartOfDay().toString,
      "end" -> s.end.atStartOfDay().toString
    )
  }
  implicit val trReads: Reads[TimeRange] =(
    (__ \ "start").read[String].map(LocalDateTime.parse(_).toLocalDate) and
    (__ \ "end").read[String].map(LocalDateTime.parse(_).toLocalDate)
  )(TimeRange)
  implicit val trFormat = Format(trReads, trWrites)
  implicit val seasonformat = Json.format[Season]
  implicit val serieformat = Json.format[Serie]
  implicit val serieWithSeasonDisplayformat = Json.format[SeasonWithSerie]
  implicit val seasonWithSeriesDisplayformat = Json.format[SerieWithSeasons]
}
