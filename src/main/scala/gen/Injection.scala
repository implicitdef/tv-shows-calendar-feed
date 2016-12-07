package gen

import java.util.concurrent.Executors

import gen.db.DbAccessor
import gen.utils.Collector

import scala.concurrent.ExecutionContext

object Injection {

  implicit val executionContext = ExecutionContext.Implicits.global
  implicit val collector = new Collector
  val theMovieDbClient = new TheMovieDbClient
  val railsClient = new RailsClient
  val railsUpdater = new RailsUpdater
  val fetchingService = new FetchingService(theMovieDbClient)
  val dbAccessor = new DbAccessor


}
