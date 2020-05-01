import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { Serie } from './myTypes'
import { FullSerie } from './myTypes'

export async function fetchAll(pagesToFetch = 100): Promise<FullSerie[]> {
  const pages = [...Array(pagesToFetch)].map((_, index) => index + 1)
  const series = (
    await Promise.all(pages.map((page) => fetchForPage(page)))
  ).flat()
  return series
}

async function fetchForPage(page: number): Promise<FullSerie[]> {
  const series = await getBestSeriesAtPage({ page })
  return Promise.all(series.map((serie) => fetchForSerie(serie)))
}

async function fetchForSerie(serie: Serie): Promise<FullSerie> {
  const seasonsNumbers = await getSeasonsNumbers(serie)
  const fullSeasons = await Promise.all(
    seasonsNumbers.map(async (season) => {
      const timeRange = await getSeasonTimeRange(serie, season)
      return {
        seasonNumber: season,
        ...timeRange,
      }
    }),
  )
  return {
    ...serie,
    seasons: fullSeasons,
  }
}
