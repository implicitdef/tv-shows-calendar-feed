import services.FetchingService
import tvshowscalendar.TvShowsCalendarClient
import utils.CS
import utils.Utils.log
import utils.Utils.threadPool
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    log("Starting the app")
    FetchingService.fetch().thenCompose { seriesWithSeasons ->
        TvShowsCalendarClient.pushData(TvShowsCalendarClient.Target.HEROKU, seriesWithSeasons)
    }
        .logIfException()
        .shutdownThreadPoolAfter()
}

fun <T> CS<T>.logIfException(): CS<T> =
    this.whenComplete { _, exception ->
        if (exception != null) {
            log(exception)
        }
    }

fun <T> CS<T>.shutdownThreadPoolAfter(): CS<T> =
    this.whenComplete { _, _ ->
        shutdown()
    }

fun shutdown() {
    log("Shutting down")
    threadPool.shutdown()
    threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
}
