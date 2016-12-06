package gen

import java.util.concurrent.Executors

import gen.utils.Collector

import scala.concurrent.ExecutionContext

object Injection {

  val executorService = Executors.newFixedThreadPool(10)
  implicit val executionContext = ExecutionContext.fromExecutor(executorService)
  implicit val collector = new Collector
  val theMovieDbClient = new TheMovieDbClient
  val railsClient = new RailsClient
  val railsUpdater = new RailsUpdater
  val fetchingService = new FetchingService(theMovieDbClient)

}
