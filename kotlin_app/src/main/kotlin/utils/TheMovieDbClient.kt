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

    object DiscoverEndpoint {
        data class Result(
            val results: List<TvShow>
        )
        data class TvShow(
            val id: Int,
            val name: String
        )
    }

    object TvShowEndpoint {
        data class TvShow(
            val seasons: List<Season>
        )
        data class Season(
            val season_number: Int
        )
    }

    object SeasonEndpoint {
        data class Season(
            val episodes: List<Episode>
        )
        data class Episode(
            val air_date: List<String>
        )
    }
    
    
    data class JsonBody(
        val origin: String
    )

    fun someHttpCall(): CompletionStage<JsonBody> =
            httpCallGeneric("http://httpbin.org/get", JsonBody::class)

    private fun <T: Any> httpCallGeneric(url: String, kClass: KClass<T>): CompletionStage<T> =
       httpCallAsString(url).thenApply { str ->
            val obj = moshi.adapter(kClass.java).fromJson(str)
            if (obj === null) {
                throw IllegalStateException("Got null from JSON parsing : $str")
            }
           obj
        }

    private fun httpCallAsString(url: String): CompletionStage<String> {
        val future = CompletableFuture<String>()
        threadPool.submit {
            Fuel.Companion.get(url).responseString{ _, _, result ->
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