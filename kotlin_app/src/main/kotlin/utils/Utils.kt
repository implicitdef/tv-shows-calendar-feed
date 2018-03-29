package utils

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors

object Utils {

    private val myLogger: Logger = LoggerFactory.getLogger("myapp")

    val threadPool = Executors.newCachedThreadPool()

    fun log(s: String) {
        myLogger.info(s)
    }

    fun log(t: Throwable) {
        myLogger.error("Caught error", t)
    }

}