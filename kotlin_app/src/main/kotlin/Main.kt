import utils.HttpServer
import themoviedb.TheMovieDbClient
import utils.Serie
import utils.Utils.log
import utils.Utils.threadPool
import java.util.concurrent.TimeUnit

/*
TODO
implement calls to themoviedb
then mash them together to have a draft of the process
then handle throttling/retry
then do upload to postgres
then rework that as a webapp with the task running periodically
*/

data class Something(val foo: List<Int>)

fun main(args: Array<String>) {
    log("Starting the app")
    doFuturesAndHttpCallsProto()
    //HttpServer.start()
}

val doServerStuff = HttpServer::start

fun doFuturesAndHttpCallsProto() {
    log("calling...")
    val TBBT = Serie(1418, "The Big Bang Theory")
    TheMovieDbClient.getBestSeriesAtPage().thenApply {
        it.forEach { (id, name) ->
            log("$id --> $name")
        }
    }.thenCompose {
        TheMovieDbClient.getSeasonsNumbers(TBBT).thenApply {
            it.forEach { n ->
                log("$TBBT S$n")
            }
        }
    }.thenCompose {
        TheMovieDbClient.getSeasonsTimeRange(TBBT, 11).thenApply {
            log("$TBBT S11 ===> $it")
        }
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