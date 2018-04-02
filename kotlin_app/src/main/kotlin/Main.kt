import services.DbService
import services.FetchingService
import services.FileStockageService
import services.JsonSerializationService.toJson
import utils.CS
import utils.HttpServer
import utils.Utils.log
import utils.Utils.threadPool
import java.util.concurrent.TimeUnit

/*
TODO
then run it fully, to produce the file
then try to upload the file to heroku, see if it works
then rework that as a webapp with the task running periodically
*/

data class Something(val foo: List<Int>)

fun main(args: Array<String>) {
    log("Starting the app")
    fetchAllAndWriteToFile()
    //fetchALittleBitAndInsertLocally()
    //doServerStuff()
}

val doServerStuff = HttpServer::start


fun fetchAllAndWriteToFile() {

    FetchingService.fetch().thenApply { seriesWithSeasons ->
        FileStockageService.writeToFile(toJson(seriesWithSeasons))
    }.handleMainCompletion()
}


fun fetchALittleBitAndInsertLocally() {
    FetchingService.fetch(pagesToFetch = 1, maxSeriesPerPage = 3).thenApply { seriesWithSeasons ->
        DbService.deleteAllJsonData()
        DbService.insertJson(DbService.Target.LOCAL, toJson(seriesWithSeasons))
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