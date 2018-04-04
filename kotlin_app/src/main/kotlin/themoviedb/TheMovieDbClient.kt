package themoviedb

import services.HttpService
import utils.CS
import utils.Serie
import utils.TimeRange
import utils.Utils.log
import java.time.LocalDate
import java.time.format.DateTimeParseException

object TheMovieDbClient {

    private val apiKey = "000ffc8b6e767158ff5489a8daba11c2"
    private val baseUrl = "https://api.themoviedb.org/3"


    fun getBestSeriesAtPage(page: Int = 1): CS<List<Serie>> =
        HttpService.httpGetThrottled(
            "$baseUrl/discover/tv",
            DiscoverEndpoint.Result::class,
            "api_key" to apiKey,
            "sort_by" to "popularity.desc",
            "page" to page.toString()
        )
            .thenApply { result ->
                result.results
                    .filterNotNull()
                    .map { Serie(it.id, it.name) }
            }

    fun getSeasonsNumbers(serie: Serie): CS<List<Int>> =
        HttpService.httpGetThrottled(
            "$baseUrl/tv/${serie.id}",
            TvShowEndpoint.TvShow::class,
            "api_key" to apiKey
        )
            .thenApply {
                it.seasons
                    .map { it.season_number }
                    .filterNot { it == 0 }
            }.exceptionally {
                log("Failed to parse the season numbers of tvShow ${serie.name}")
                emptyList()
            }

    fun getSeasonTimeRange(serie: Serie, season: Int): CS<TimeRange?> =
        HttpService.httpGetThrottled(
            "$baseUrl/tv/${serie.id}/season/$season",
            SeasonEndpoint.Season::class,
            "api_key" to apiKey
        )
            .thenApply {
                it.episodes
                    .map { it.air_date }
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
}