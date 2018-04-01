package services

import com.squareup.moshi.Types
import utils.SerieWithSeasons
import utils.Utils

object JsonSerializationService {

    fun toJson(seriesWithSeasons: List<SerieWithSeasons>): String {
        val type = Types.newParameterizedType(List::class.java, SerieWithSeasons::class.java)
        val adapter = Utils.moshi.adapter<List<SerieWithSeasons>>(type)
        return adapter.toJson(seriesWithSeasons)
    }


}