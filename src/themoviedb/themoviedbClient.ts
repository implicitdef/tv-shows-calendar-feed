import { reverse } from 'esrever'
import axios from 'axios'

const API_KEY = reverse('2c11abad8a9845ff851767e6b8cff000')
const BASE_URL = 'https://api.themoviedb.org/3'

type Serie = { id: number; name: string }
type TimeRange = {
  start: Date
  end: Date
}
type DiscoverEndpointResult = {
  results: { id: number; name: string }[]
}
type TvShowEndpointResult = {
  seasons: { season_number: number }[]
}
type SeasonEndpointResult = {
  episodes: { air_date: string }[]
}

// TODO ajouter throttling sur tous les appels
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
  const {
    data: { seasons },
  } = await axios.get<TvShowEndpointResult>(`${BASE_URL}/tv/${serie.id}`, {
    params: {
      api_key: API_KEY,
    },
  })
  return seasons.map((_) => _.season_number)
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
    // TODO revoir ce parsing, c'est probablement pas fonctionnel
    ({ air_date }) => new Date(air_date),
  )
  return { start, end }
}
