import services.FetchingService
import utils.HttpServer
import utils.Utils.log
import utils.Utils.threadPool
import java.util.concurrent.DelayQueue
import java.util.concurrent.TimeUnit

/*
TODO
fix the throttler : it limits to N calls at once, but not to N calls in M time
https://stackoverflow.com/questions/1407113/throttling-method-calls-to-m-requests-in-n-seconds
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
    FetchingService.fetch(pageToFetch = 2).thenApply { seriesWithSeasons ->
        log("Fetched ${seriesWithSeasons.size} seasons")
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