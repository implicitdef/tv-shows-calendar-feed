package utils

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.FuelManager
import com.github.kittinunf.fuel.core.Response
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.result.Result
import com.google.gson.Gson
import utils.Utils.threadPool
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

object TheMovieDbClient {

    private val apiKey = "000ffc8b6e767158ff5489a8daba11c2"
    private val baseUrl = "https://api.themoviedb.org/3"


    data class JsonBody(
            val origin: String
    ) {
        class Deserializer : ResponseDeserializable<JsonBody> {
            override fun deserialize(content: String) =
                Gson().fromJson(content, JsonBody::class.java)
        }
    }


    fun dummyCallWithCompletionStage(): CompletionStage<JsonBody> =
        httpCall("http://httpbin.org/get")

    private fun httpCall(url: String): CompletionStage<JsonBody> {
        val future = CompletableFuture<JsonBody>()
        threadPool.submit {
            Fuel.Companion.get(url).responseObject(JsonBody.Deserializer()) { _, _, result ->
                when (result) {
                    is Result.Failure -> {
                        future.completeExceptionally(result.getException())
                    }
                    is Result.Success -> {
                        future.complete(result.get())
                    }
                }
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