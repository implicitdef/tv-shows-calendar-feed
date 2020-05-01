import { reverse } from 'esrever'
import axios from 'axios'
import { Serie, TimeRange } from './myTypes'
import { firstAndLast, isDefined } from './utils'

const API_KEY = reverse('2c11abad8a9845ff851767e6b8cff000')
const BASE_URL = 'https://api.themoviedb.org/3'

type DiscoverEndpointResult = {
  results: { id: number; name: string }[]
}
type TvShowEndpointResult = {
  seasons: { season_number: number }[]
}
type SeasonEndpointResult = {
  episodes: { air_date: string | null }[]
}

export async function getBestSeriesAtPage({ page = 1 } = {}): Promise<Serie[]> {
  const {
    data: { results },
  } = await axios.get<DiscoverEndpointResult>(`${BASE_URL}/discover/tv`, {
    params: {
      api_key: API_KEY,
      sort_by: 'popularity.desc',
      page,
    },
  })
  return results
}

export async function getSeasonsNumbers(serie: Serie): Promise<number[]> {
  const { data } = await axios.get<TvShowEndpointResult>(
    `${BASE_URL}/tv/${serie.id}`,
    {
      params: {
        api_key: API_KEY,
        append_to_response: 'seasdons',
      },
    },
  )
  return data.seasons.map((_) => _.season_number)
}

export async function getSeasonTimeRange(
  serie: Serie,
  season: number,
): Promise<TimeRange> {
  const {
    data: { episodes },
  } = await axios.get<SeasonEndpointResult>(
    `${BASE_URL}/tv/${serie.id}/season/${season}`,
    {
      params: {
        api_key: API_KEY,
      },
    },
  )
  const airDates = episodes.map((_) => _.air_date).filter(isDefined)
  if (airDates.length) {
    const firstAndLastAirDate = firstAndLast(airDates)
    const [start, end] = firstAndLastAirDate.map((airDate) => {
      if (airDate.match(/^\d{4}-\d{2}-\d{2}$/)) {
        return new Date(`${airDate}T00:00:00Z`)
      }
      throw new Error(
        `Unexpected episode date for serie ${serie.name} ${serie.id} at season ${season}: ${airDate}`,
      )
    })
    return { start, end }
  }
  throw new Error(
    `No valid episodes for serie ${serie.name} ${serie.id} at season ${season}`,
  )
}
