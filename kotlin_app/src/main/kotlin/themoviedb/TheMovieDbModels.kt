package themoviedb

object DiscoverEndpoint {
    data class Result(
        val results: List<TvShow?>
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
        val air_date: String?
    )
}