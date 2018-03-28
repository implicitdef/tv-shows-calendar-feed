package utils

import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import utils.Utils.threadPool
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.concurrent.Future
import javax.annotation.processing.Completion

object TheMovieDbClient {

    private val apiKey = "000ffc8b6e767158ff5489a8daba11c2"
    private val baseUrl = "https://api.themoviedb.org/3"

    fun dummyCall(callback: (String) -> Unit) {
        "http://httpbin.org/get".httpGet().responseString { request, response, result ->
            when (result) {
                is Result.Failure -> {
                    throw result.getException()
                }
                is Result.Success -> {
                    callback(result.get())
                }
            }
        }
    }

    fun dummyCallWithCompletionStage(): CompletionStage<String> {
        val future = CompletableFuture<String>()
        threadPool.submit { ->
            dummyCall { body ->
                future.complete(body)
            }
        }
        return future
    }



    /*

    def getBestSeriesAtPage(page: Int = 1)(implicit e: ExecutionContext, c: Collector): Future[Seq[Serie]] =
    callWithBackoff(s"$baseUrl/discover/tv", "sort_by" -> "popularity.desc", "page" -> page.toString).map {
      _
        .as[DiscoverEndpoint.Result]
        .results.map(tvShow => Serie(tvShow.id, tvShow.name))
    }


     */


}