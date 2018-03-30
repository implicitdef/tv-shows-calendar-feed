package utils

import java.time.LocalDate

data class Serie(
    val id: Int,
    val name: String
)
data class Season(
    val number: Int,
    val time: TimeRange
)
data class TimeRange(
    val start: LocalDate,
    val end: LocalDate
)
data class SeasonWithSerie(
    val serie: Serie,
    val number: Int,
    val time: TimeRange
)
data class SerieWithSeasons(
    val serie: Serie,
    val seasons: List<Season>
)