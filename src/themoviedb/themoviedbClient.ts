import { reverse } from 'esrever'
import axios from 'axios'
import { Serie, TimeRange } from '../myTypes'

const API_KEY = reverse('2c11abad8a9845ff851767e6b8cff000')
const BASE_URL = 'https://api.themoviedb.org/3'

type DiscoverEndpointResult = {
  results: { id: number; name: string }[]
}
type TvShowEndpointResult = {
  seasons: { season_number: number }[]
}
type SeasonEndpointResult = {
  episodes: { air_date: string }[]
}

// TODO gérer tous les cas foireux que je gérais en Kotlin pour être fault tolerant

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

  const [start, end] = [episodes[0], episodes[episodes.length - 1]].map(
    ({ air_date }) => {
      if (!air_date.match(/^\d{4}-\d{2}-\d{2}$/)) {
        throw new Error(
          `Unexpected episode date :  ${serie.id}, ${serie.name}, ${season}, ${air_date}`,
        )
      }
      return new Date(`${air_date}T00:00:00Z`)
    },
  )
  return { start, end }
}
