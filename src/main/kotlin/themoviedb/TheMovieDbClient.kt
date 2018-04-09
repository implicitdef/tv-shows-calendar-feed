package themoviedb

import org.funktionale.either.Either.Left
import org.funktionale.either.Either.Right
import services.HttpService
import utils.CS
import utils.Serie
import utils.TimeRange
import utils.Utils.warn
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
            .thenApply {
                when (it) {
                    is Right ->
                        it.right().get().results
                            .filterNotNull()
                            .map { Serie(it.id, it.name) }
                    is Left -> {
                        warn("NotFound when fetching series at page $page")
                        emptyList()
                    }
                }
            }

    fun getSeasonsNumbers(serie: Serie): CS<List<Int>> =
        HttpService.httpGetThrottled(
            "$baseUrl/tv/${serie.id}",
            TvShowEndpoint.TvShow::class,
            "api_key" to apiKey
        )
            .thenApply {
                when (it) {
                    is Right ->
                        it.right().get().seasons
                            .map { it.season_number }
                            .filterNot { it == 0 }
                    is Left -> {
                        warn("NotFound when fetching season numbers of serie $serie")
                        emptyList()
                    }
                }
            }.exceptionally {
                warn("Failed to parse the season numbers of tvShow $serie")
                emptyList()
            }

    fun getSeasonTimeRange(serie: Serie, season: Int): CS<TimeRange?> =
        HttpService.httpGetThrottled(
            "$baseUrl/tv/${serie.id}/season/$season",
            SeasonEndpoint.Season::class,
            "api_key" to apiKey
        )
            .thenApply {
                when (it) {
                    is Right ->
                        it.right().get().episodes
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
                    is Left -> {
                        warn("NotFound when fetching season $season of serie $serie")
                        emptyList()
                    }
                }
            }.thenApply {
                if (it.isEmpty()) {
                    warn("${serie.name} season $season has zero episodes")
                    null
                } else {
                    TimeRange(it.first(), it.last())
                }
            }
}