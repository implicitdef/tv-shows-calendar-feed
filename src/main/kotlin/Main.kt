import services.FetchingService
import services.JsonSerializationService.toJson
import tvshowscalendar.TvShowsCalendarClient
import utils.CS
import utils.HttpServer
import utils.Scheduler
import utils.Utils.log
import utils.Utils.threadPool
import java.io.File
import java.util.concurrent.TimeUnit

/*
TODO monitor execution on heroku
*/

fun main(args: Array<String>) {
    log("Starting the app")
    Scheduler.schedule {
        log("Starting the fetch/push")
        FetchingService.fetch().thenCompose { seriesWithSeasons ->
            TvShowsCalendarClient.pushData(TvShowsCalendarClient.Target.HEROKU, seriesWithSeasons)
        }
    }
    HttpServer.start()
}

fun fetchAllAndSendToHerokuNodAndKillThreadPool() {
    FetchingService.fetch().thenCompose { seriesWithSeasons ->
        TvShowsCalendarClient.pushData(TvShowsCalendarClient.Target.HEROKU, seriesWithSeasons)
    }.handleMainCompletion()
}

fun <T> CS<T>.handleMainCompletion() =
    this.whenComplete { _, exception ->
        if (exception != null) {
            log(exception)
        }
        shutdown()
    }

fun shutdown() {
    log("Shutting down")
    threadPool.shutdown()
    threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS)
}
