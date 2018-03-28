import utils.TheMovieDbClient
import utils.Utils.log
import utils.Utils.threadPool
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    log("Starting the app")
    //HttpServer.start()
    log("calling...")
    TheMovieDbClient.dummyCallWithCompletionStage().thenApply { body ->
        log("call completed but still waiting in callback")
        Thread.sleep(3000)
        log("call completed with result $body")
    }.thenApply { shutdown() }
}

fun shutdown() {
    log("Shutting down")
    threadPool.shutdown()
    threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
}