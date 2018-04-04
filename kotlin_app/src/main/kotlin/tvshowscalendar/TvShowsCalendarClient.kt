package tvshowscalendar

import services.HttpService
import services.JsonSerializationService
import utils.CS
import utils.SerieWithSeasons

object TvShowsCalendarClient {


    const val tvShowsCalendarUrl = "http://localhost:3333"
    const val tvShowsCalendarPushDataApiKey = "pushDataApiKey"

    enum class Target(
        val url: String,
        val apiKey: String
    ) {
        LOCAL(
            url = "http://localhost:3333",
            apiKey = "pushDataApiKey"
        ),
        HEROKU(
            url = "https://tv-shows-calendar.herokuapp.com",
            apiKey = "pushDataApiKey"
        )
    }

    fun pushData(target: Target, data: String): CS<Unit> =
        HttpService.httpPost(
            target.url + "/data",
            data,
            "key" to target.apiKey
        )

    fun pushData(target: Target, data: List<SerieWithSeasons>): CS<Unit> =
        pushData(target, JsonSerializationService.toJson(data))

}