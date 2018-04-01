import com.squareup.moshi.Types
import services.FetchingService
import services.JsonSerializationService.toJson
import utils.HttpServer
import utils.SerieWithSeasons
import utils.Utils.log
import utils.Utils.moshi
import utils.Utils.threadPool
import java.util.concurrent.DelayQueue
import java.util.concurrent.TimeUnit

/*
TODO
then do upload to postgres
then rework that as a webapp with the task running periodically
*/

data class Something(val foo: List<Int>)

fun main(args: Array<String>) {
    log("Starting the app")
    doSomeKindOfFetching()
    //doServerStuff()
}

val doServerStuff = HttpServer::start

fun doSomeKindOfFetching() {
    log("calling...")
    FetchingService.fetch(pageToFetch = 1, maxSeriesPerPage = 3).thenApply { seriesWithSeasons ->
        log("Fetched ${seriesWithSeasons.size} seasons")
        log(toJson(seriesWithSeasons).take(100) + "...")
    }.whenComplete { _, exception ->
        if (exception != null) {
            log(exception)
        }
        shutdown()
    }
}

fun shutdown() {
    log("Shutting down")
    threadPool.shutdown()
    threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
}