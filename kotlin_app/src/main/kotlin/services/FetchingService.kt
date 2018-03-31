package services

import themoviedb.TheMovieDbClient
import utils.CS
import utils.Season
import utils.Serie
import utils.SerieWithSeasons
import utils.Utils.sequence

object FetchingService {

    private val client = TheMovieDbClient

    fun fetch(pageToFetch: Int = 100): CS<List<SerieWithSeasons>> =
        (1..pageToFetch)
            .map(::fetchPage)
            .sequence()
            .thenApply { it.flatten() }



    private fun fetchPage(pageNumber: Int): CS<List<SerieWithSeasons>> =
        client.getBestSeriesAtPage(pageNumber)
            .thenCompose { series ->
                series
                    .map(::fetchForSerie)
                    .sequence()
            }

    private fun fetchForSerie(serie: Serie): CS<SerieWithSeasons> =
        client
            .getSeasonsNumbers(serie)
            .thenCompose { seasonNumbers ->
                seasonNumbers.map { seasonNumber ->
                    client.getSeasonTimeRange(serie, seasonNumber)
                        .thenApply { timeRange ->
                            timeRange?.let { Season(seasonNumber, it) }
                        }
                }.sequence()
                    .thenApply { seasons ->
                        SerieWithSeasons(serie, seasons.filterNotNull())
                    }
            }
}