package utils

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.ToJson
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeParseException
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Executors

typealias CS<T> = CompletionStage<T>

object Utils {

    private val myLogger: Logger = LoggerFactory.getLogger("myapp")

    val threadPool = Executors.newCachedThreadPool()

    object LocalDateJsonAdapter {
        @FromJson
        fun fromJson(str: String): LocalDate =
            try {
                LocalDateTime.parse(str).toLocalDate()
            } catch (e: DateTimeParseException) {
                throw JsonDataException(e)
            }

        @ToJson
        fun toJson(localDate: LocalDate): String =
            localDate.atStartOfDay().toString()
    }

    val moshi = Moshi.Builder()
        .add(LocalDateJsonAdapter)
        .add(KotlinJsonAdapterFactory())
        .build()

    fun log(s: String) {
        myLogger.info(s)
    }

    fun log(a: Any) {
        log(a.toString())
    }

    fun log(t: Throwable) {
        myLogger.error("Caught error", t)
    }

    // Equivalent to Promise.sequence in JS or .sequence in Scala
    fun <T> List<CS<T>>.sequence(): CS<List<T>> {
        val listOfCF = this.map { it.toCompletableFuture() }
        return CompletableFuture
            .allOf(*listOfCF.toTypedArray())
            .thenApply {
                listOfCF.map { it.join() }
            }
    }

    fun <T> measureTime(block: () -> CS<T>): CS<Pair<T, Duration>> {
        val start = System.currentTimeMillis()
        return block().thenApply { result ->
            Pair(result, Duration.ofMillis(System.currentTimeMillis() - start))
        }
    }

    fun Duration.toHumanReadableString(): String {
        val nbHours = this
            .toHours()
        val nbMinutes = this
            .minusHours(nbHours)
            .toMinutes()
        val nbSeconds = (this
            .minusHours(nbHours)
            .minusMinutes(nbMinutes)
            .toMillis().toDouble() / 1000
            )
        return String.format("%dh %dmin %.2fs", nbHours, nbMinutes, nbSeconds)
    }
}
