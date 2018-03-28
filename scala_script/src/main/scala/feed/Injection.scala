package feed

import feed.db.{ClassicDbAccessor, NeoDbAccessor}
import feed.utils.Collector

import scala.concurrent.ExecutionContext

object Injection {

  implicit val executionContext = ExecutionContext.Implicits.global
  implicit val collector = new Collector
  val theMovieDbClient = new TheMovieDbClient
  val railsUpdater = new RailsUpdater
  val fetchingService = new FetchingService(theMovieDbClient)
  val classicDbAccessor = new ClassicDbAccessor
  val neoDbAccessor = new NeoDbAccessor


}
