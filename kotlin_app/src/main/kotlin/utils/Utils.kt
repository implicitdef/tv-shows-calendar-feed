package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors

typealias CS<T> = CompletionStage<T>

object Utils {

    private val myLogger: Logger = LoggerFactory.getLogger("myapp")

    val threadPool = Executors.newCachedThreadPool()

    fun log(s: String) {
        myLogger.info(s)
    }

    fun log(a: Any) {
        log(a.toString())
    }

    fun log(t: Throwable) {
        myLogger.error("Caught error", t)
    }


}
