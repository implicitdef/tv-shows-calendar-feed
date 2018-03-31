package services

import themoviedb.TheMovieDbClient
import utils.CS
import utils.Serie
import utils.SerieWithSeasons
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage

class FetchingService {

    val client = TheMovieDbClient

    /*


    private def fetchPage(pageNumber: Int)
                       (implicit e: ExecutionContext,
                        c: Collector): Future[Seq[SerieWithSeasons]] =
    client.getBestSeriesAtPage(pageNumber).flatMap { series =>
      series.map(fetchForSerie).sequence
    }


    private def fetchForSerie(serie: Serie)
                           (implicit e: ExecutionContext,
                            c: Collector): Future[SerieWithSeasons] =
    client.getSeasonsNumbers(serie).flatMap { seasonsNumber =>
      seasonsNumber.map { seasonNumber =>
        client.getSeasonsTimeRange(serie, seasonNumber).map {
          _.map { time =>
            Season(seasonNumber, time)
          }
        }
      }.sequence
       .map(_.flatten)
       .map(seasons => SerieWithSeasons(serie, seasons))
    }
     */

    // private fun fetchPage(pageNumber: Int): CS<List<SerieWithSeasons>> =
    //     client.getBestSeriesAtPage(pageNumber)
    //         .thenCompose {
    //             it.map {
    //                 client.
    //             }
    //
    //         }

    // private fun fetchForSerie(serie: Serie): CS<SerieWithSeasons> =
    //     client.getSeasonsNumbers(serie)
    //         .thenCompose {
    //             val futureOfTimesRanges = it.map {
    //                 client.getSeasonsTimeRange(serie, it).toCompletableFuture()
    //             }
    //             CompletableFuture.allOf(
    //                 *futureOfTimesRanges.toTypedArray()
    //             )
    //             TODO()
    //         }
}