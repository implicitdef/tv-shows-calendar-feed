package feed

import feed.db.DbAccessor
import feed.utils.Collector

import scala.concurrent.ExecutionContext

object Injection {

  implicit val executionContext = ExecutionContext.Implicits.global
  implicit val collector = new Collector
  val theMovieDbClient = new TheMovieDbClient
  val railsUpdater = new RailsUpdater
  val fetchingService = new FetchingService(theMovieDbClient)
  val dbAccessor = new DbAccessor


}
