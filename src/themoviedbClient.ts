import { reverse } from 'esrever'
import axios from 'axios'
import { Serie, TimeRange } from './myTypes'
import { firstAndLast, isDefined, keepOnlyKeys, timeoutPromise } from './utils'
import Bottleneck from 'bottleneck'

type DiscoverEndpointResult = {
  results: { id: number; name: string }[]
}
type TvShowEndpointResult = {
  seasons: { season_number: number }[]
}
type SeasonEndpointResult = {
  episodes: { air_date: string | null }[]
}

const API_KEY = reverse('2c11abad8a9845ff851767e6b8cff000')
const BASE_URL = 'https://api.themoviedb.org/3'

// In theory there is no rate limit on the API
// in practice if we go too fast the server doesn't answer anymore
const limiter = new Bottleneck({
  maxConcurrent: 10,
  minTime: 20,
})

async function withRetry<A>(
  func: () => Promise<A>,
  { nbTries = 0, timeoutBeforeRetry = 100 } = {},
): Promise<A> {
  try {
    if (nbTries > 0) console.log(`Retrying something for the ${nbTries}th time`)
    return await func()
  } catch (e) {
    if (e.message.includes('timeout')) {
      await timeoutPromise(timeoutBeforeRetry)
      return withRetry(func, {
        nbTries: nbTries + 1,
        // timeout increases exponentially, with a bit of randomness
        timeoutBeforeRetry: timeoutBeforeRetry * (2 + Math.random()),
      })
    }
    throw e
  }
}

async function call<R>(
  log: string,
  path: string,
  params: any = {},
): Promise<R> {
  // We're unable to find a perfect rate for the limiter
  // we will still get timeouts from time to time
  // so we put a retry logic on top of it
  return withRetry(async () => {
    return limiter.schedule(async () => {
      console.log(log)
      const { data } = await axios.get<R>(`${BASE_URL}${path}`, {
        timeout: 1000,
        params: {
          api_key: API_KEY,
          ...params,
        },
      })
      return data
    })
  })
}

export async function getBestSeriesAtPage({ page = 1 } = {}): Promise<Serie[]> {
  const { results } = await call<DiscoverEndpointResult>(
    `>> discover page ${page}`,
    '/discover/tv',
    {
      sort_by: 'popularity.desc',
      page,
    },
  )
  return results.map((serie) => keepOnlyKeys(serie, 'id', 'name'))
}

export async function getSeasonsNumbers(serie: Serie): Promise<number[]> {
  console.log('>>>> get seasons numbers of ', serie.id, serie.name)
  const { seasons } = await call<TvShowEndpointResult>(
    `>>>> get seasons numbers of ${serie.id} ${serie.name}`,
    `/tv/${serie.id}`,
  )
  return seasons.map((_) => _.season_number)
}

export async function getSeasonTimeRange(
  serie: Serie,
  season: number,
): Promise<TimeRange> {
  const { episodes } = await call<SeasonEndpointResult>(
    `>>>>>> get season time range ${serie.id} ${serie.name} S${season}`,
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
