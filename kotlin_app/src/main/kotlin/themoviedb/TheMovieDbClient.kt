package themoviedb

import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.result.Result
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import utils.CS
import utils.Serie
import utils.TimeRange
import utils.Utils.log
import utils.Utils.threadPool
import java.time.LocalDate
import java.time.format.DateTimeParseException
import java.util.concurrent.CompletableFuture
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

    fun getBestSeriesAtPage(page: Int = 1): CS<List<Serie>> =
        httpCallGeneric(
            "/discover/tv",
            DiscoverEndpoint.Result::class,
            "sort_by" to "popularity.desc",
            "page" to page.toString()
        )
            .thenApply { result ->
                result.results.map { Serie(it.id, it.name) }
            }

    fun getSeasonsNumbers(serie: Serie): CS<List<Int>> =
        httpCallGeneric(
            "/tv/${serie.id}",
            TvShowEndpoint.TvShow::class
        )
            .thenApply {
                it.seasons
                    .map { it.season_number }
                    .filterNot { it == 0 }
            }.exceptionally {
                log("Failed to parse the season numbers of tvShow ${serie.name}")
                emptyList()
            }

    fun getSeasonsTimeRange(serie: Serie, season: Int): CS<TimeRange?> =
        httpCallGeneric(
            "/tv/${serie.id}/season/$season",
            SeasonEndpoint.Season::class
        )
            .thenApply {
                it.episodes
                    .map { it.air_date}
                    .filterNotNull()
                    .map {
                        try {
                            LocalDate.parse(it)
                        } catch (e: DateTimeParseException) {
                            null
                        }
                    }
                    .filterNotNull()
                    .sorted()
            }.thenApply {
                if (it.isEmpty()) {
                    log("${serie.name} season $season has zero episodes")
                    null
                } else {
                    TimeRange(it.first(), it.last())
                }
            }

    private fun <T : Any> httpCallGeneric(
        path: String,
        kClass: KClass<T>,
        vararg extraParams: Pair<String, String>
    ): CS<T> =
        httpCallAsString(path, *extraParams).thenApply { str ->
            val obj = moshi.adapter(kClass.java).fromJson(str)
            if (obj === null) {
                throw IllegalStateException("Got null from JSON parsing : $str")
            }
            obj
        }

    private fun httpCallAsString(
        path: String,
        vararg extraParams: Pair<String, String>
    ): CS<String> {
        val future = CompletableFuture<String>()
        val params = listOf("api_key" to apiKey).plus(extraParams)
        val url = "$baseUrl$path"
        log(">> $url")
        threadPool.submit {
            Fuel.Companion
                .get(url, params)
                .responseString { _, response, result ->
                    log("<< ${response.statusCode}")
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
}