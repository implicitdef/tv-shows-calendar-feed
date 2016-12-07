package gen

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors

import gen.Domain._
import gen.utils.Collector
import gen.utils.Pimp._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

object Main {

  object RunConfig {
    val doTheFetchingAndWritingToFile: Boolean = false
    val doTheReadingFileAndUpdatingRailsApp: Boolean = true
    val printThrowablesCollected: Boolean = false
    val pagesToBeFetched: Int = 100
    // we observed that 1000 series means ~7160 rows to store (series + seasons)
    // this way we stay under heroku free postgres limit (10k rows)
    val maxSeriesToStore: Int = 1000
  }

  import Injection._

  def main(args: Array[String]): Unit = {
    launch
  }

  def launch: Unit = {
    try {
      logger(this).info("------ Starting ------")
      if (RunConfig.doTheFetchingAndWritingToFile) {
        logger(this).info("--- Fetching...")
        val seriesWithSeasons = fetchingService.fetch(pagesToFetch = RunConfig.pagesToBeFetched).await()
        logger(this).info("--- Fetching done")
        logger(this).info("--- Writing the JSON to the file...")
        val jsonStr = Json.stringify(Json.toJson(seriesWithSeasons))
        Files.write(Paths.get("data.json"), jsonStr.getBytes(StandardCharsets.UTF_8))
        logger(this).info("--- Writing the JSON done")
      }
      if (RunConfig.doTheReadingFileAndUpdatingRailsApp) {
        logger(this).info("--- Reading the JSON from the file...")
        val seriesWithSeasons = Json.parse(scala.io.Source.fromFile("data.json").mkString).as[Seq[SerieWithSeasons]]
        logger(this).info("--- Reading the JSON done")
        logger(this).info("--- Updating the rails app...")
        railsUpdater.updateRails(seriesWithSeasons.take(RunConfig.maxSeriesToStore)).await()
        logger(this).info("--- Updating the rails app done")
      }
      if (RunConfig.printThrowablesCollected) {
        logger(this).info("--- Printing collected throwables...")
        collector.throwables.foreach { t =>
          logger(this).info(t.getMessage)
        }
        logger(this).info("--- Printing collected throwables done")
      }
    } catch {
      case NonFatal(t) =>
        logger(this).error("Something went wrong", t)
    } finally {
      theMovieDbClient.shutdown()
      railsClient.shutdown()
    }
    logger(this).info("------ All done -------")
  }


}
