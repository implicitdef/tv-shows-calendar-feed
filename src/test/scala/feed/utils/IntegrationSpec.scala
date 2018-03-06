package feed.utils


import feed.Injection
import feed.utils.Pimp._
import org.scalatest.{FreeSpec, Matchers}

import scala.concurrent.duration._

class IntegrationSpec extends FreeSpec with Matchers {

  import Injection._

  "FetchingService" - {
    "should be able to fetch a page" in {
      val seriesWithSeasons = fetchingService.fetch(1).await(2.minutes)
      seriesWithSeasons.size should be >= 1
    }
    "should be able to fetch a few pages" in {
      val seriesWithSeasons = fetchingService.fetch(5).await(10.minutes)
      seriesWithSeasons.size should be >= 1
    }
  }



}
