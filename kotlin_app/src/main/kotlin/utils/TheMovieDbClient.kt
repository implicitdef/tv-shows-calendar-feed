package utils

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.core.ResponseDeserializable
import com.github.kittinunf.result.Result
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import utils.Utils.threadPool
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import kotlin.reflect.KClass

object TheMovieDbClient {

    private val apiKey = "000ffc8b6e767158ff5489a8daba11c2"
    private val baseUrl = "https://api.themoviedb.org/3"
    private val moshi = Moshi.Builder()
            .add(KotlinJsonAdapterFactory())
            .build()

    data class JsonBody(
        val origin: String
    )

    fun dummyCallWithCompletionStage(): CompletionStage<JsonBody> =
            httpCallGeneric("http://httpbin.org/get", JsonBody::class)

    private fun <T: Any> httpCallGeneric(url: String, kClass: KClass<T>): CompletionStage<T> {
        val future = CompletableFuture<T>()
        val deserializer = object : ResponseDeserializable<T> {
            override fun deserialize(content: String) =
                moshi.adapter(kClass.java).fromJson(content)
        }
        threadPool.submit {
            Fuel.Companion.get(url).responseObject(deserializer) { _, _, result ->
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