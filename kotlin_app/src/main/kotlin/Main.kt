import utils.HttpServer
import themoviedb.TheMovieDbClient
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
    TheMovieDbClient.getBestSeriesAtPage().thenApply { result ->
        log("call completed with result $result")
        result.forEach { (_, name) ->
            log("--> $name")
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