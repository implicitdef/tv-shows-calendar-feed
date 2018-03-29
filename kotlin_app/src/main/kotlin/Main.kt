import utils.HttpServer
import utils.TheMovieDbClient
import utils.Utils.log
import utils.Utils.threadPool
import java.util.concurrent.TimeUnit


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