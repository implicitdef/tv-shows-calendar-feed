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
TODO

later :
transform this into a webapp, with the task running once a day at 3:00 am
deploy to heroku
*/

val file = File("data.json")

fun main(args: Array<String>) {
    log("Starting the app")
    //Scheduler.schedule {
    //    log("Doing something")
    //}
    //Thread.sleep(Long.MAX_VALUE)
    //fetchALittleAndSendToLocalNode()
    //fetchAllAndSendToHerokuNode()
    //fetchAllAndWriteToFile()
    //readFileAndUploadTHerokuNode()
    doServerStuff()
}

val doServerStuff = HttpServer::start

fun fetchALittleAndSendToLocalNode() {
    FetchingService.fetch(pagesToFetch = 1, maxSeriesPerPage = 2).thenCompose { seriesWithSeasons ->
        TvShowsCalendarClient.pushData(TvShowsCalendarClient.Target.LOCAL, seriesWithSeasons)
    }.handleMainCompletion()
}

fun readFileAndUploadTHerokuNode() {
    val data = file.readText()
    TvShowsCalendarClient.pushData(TvShowsCalendarClient.Target.HEROKU, data)
        .handleMainCompletion()
}

fun fetchAllAndWriteToFile() {
    FetchingService.fetch().thenApply { seriesWithSeasons ->
        file.writeText(toJson(seriesWithSeasons))
    }.handleMainCompletion()
}

fun fetchAllAndSendToHerokuNode() {
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
