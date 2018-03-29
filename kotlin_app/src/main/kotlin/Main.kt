import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import utils.HttpServer
import utils.TheMovieDbClient
import utils.Utils.log
import utils.Utils.threadPool
import java.lang.Math.random
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
    TheMovieDbClient.dummyCallWithCompletionStage().thenApply { body ->
        log("call completed with result $body")
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