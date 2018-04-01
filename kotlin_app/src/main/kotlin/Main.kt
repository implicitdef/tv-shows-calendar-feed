import com.squareup.moshi.Types
import services.DbService
import services.DbService.deleteAllJsonData
import services.FetchingService
import services.JsonSerializationService.toJson
import utils.HttpServer
import utils.SerieWithSeasons
import utils.Utils.log
import utils.Utils.moshi
import utils.Utils.threadPool
import java.util.concurrent.DelayQueue
import java.util.concurrent.TimeUnit

/*
TODO
then do upload to postgres
then put logs on interactions with the db
then test locally if the json produced works with the frontend
then write code to put it to a file
then write code to add a log of execution time at the end
then run it fully, to produce the file
then try to upload the file to heroku, see if it works
then rework that as a webapp with the task running periodically
*/

data class Something(val foo: List<Int>)

fun main(args: Array<String>) {
    log("Starting the app")
    fetchALittleBitAndInsertLocally()
    //doServerStuff()
}

val doServerStuff = HttpServer::start


fun fetchALittleBitAndInsertLocally() {
    FetchingService.fetch(pageToFetch = 1, maxSeriesPerPage = 3).thenApply { seriesWithSeasons ->
        DbService.deleteAllJsonData()
        DbService.insertJson(DbService.Target.LOCAL, toJson(seriesWithSeasons))
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