package gen

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Paths}
import java.util.concurrent.Executors

import gen.Domain._
import gen.utils.Collector
import gen.utils.Pimp._
import play.api.libs.json.Json

import scala.concurrent.{ExecutionContext, Future}

object Main {

  import Injection._

  def main(args: Array[String]): Unit = {
    launch
  }

  def launch: Unit = {
    try {
      logger(this).info("Querying...")
      val seriesWithSeasons = fetchingService.fetch(pagesToFetch = 3).await()
      logger(this).info("Writing the JSON to the file...")
      val jsonStr = Json.stringify(Json.toJson(seriesWithSeasons))
      Files.write(Paths.get("data.json"), jsonStr.getBytes(StandardCharsets.UTF_8))


      //logger(this).info("Reading the JSON from the file...")
      //val seriesWithSeasons = Json.parse(scala.io.Source.fromFile("data.json").mkString).as[Seq[SerieWithSeasons]]


      logger(this).info("Updating the rails app...")
      railsUpdater.updateRails(seriesWithSeasons).await()
      printCollectedThrowables(collector)
      logger(this).info("Done")
    }
    finally {
      client.shutdown()
      executorService.shutdown()
    }
  }

  private def printCollectedThrowables(collector: Collector) = {
    logger(this).info(s"--- Warnings ---")
    collector.throwables.foreach { t =>
      logger(this).info(t.getMessage)
    }
  }

}
