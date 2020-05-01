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

async function call<R>(path: string, params: any = {}): Promise<R> {
  const { data } = await axios.get<R>(`${BASE_URL}${path}`, {
    params: {
      api_key: API_KEY,
      ...params,
    },
  })
  return data
}

export async function getBestSeriesAtPage({ page = 1 } = {}): Promise<Serie[]> {
  console.log('>> discover page', page)
  const { results } = await call<DiscoverEndpointResult>('/discover/tv', {
    sort_by: 'popularity.desc',
    page,
  })
  return results
}

export async function getSeasonsNumbers(serie: Serie): Promise<number[]> {
  console.log('>>>> get seasons numbers of ', serie.id, serie.name)
  const { seasons } = await call<TvShowEndpointResult>(`/tv/${serie.id}`)
  return seasons.map((_) => _.season_number)
}

export async function getSeasonTimeRange(
  serie: Serie,
  season: number,
): Promise<TimeRange> {
  console.log(
    '>>>>>> get season time range ',
    serie.id,
    serie.name,
    `S${season}`,
  )
  const { episodes } = await call<SeasonEndpointResult>(
    `/tv/${serie.id}/season/${season}`,
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
