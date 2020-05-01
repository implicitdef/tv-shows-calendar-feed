import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { Serie } from './myTypes'
import { FullSerie } from './myTypes'
import { isDefined } from './utils'

export async function fetchAll(pagesToFetch = 100): Promise<FullSerie[]> {
  const pages = [...Array(pagesToFetch)].map((_, index) => index + 1)
  const series = (
    await Promise.all(pages.map((page) => fetchForPage(page)))
  ).flat()
  console.log(`<< Success, got ${series.length}`)
  return series
}

async function fetchForPage(page: number): Promise<FullSerie[]> {
  const series = await getBestSeriesAtPage({ page })
  return (
    await Promise.all(
      series.map(async (serie) => {
        try {
          const fullSerie = await fetchForSerie(serie)
          console.log(
            '<<<< Success for serie',
            fullSerie.id,
            fullSerie.name,
            `(${fullSerie.seasons.length} seasons)`,
          )
          return fullSerie
        } catch (e) {
          console.log(
            `Discarding serie ${serie.id} ${serie.name}: ${e.message}`,
          )
          return null
        }
      }),
    )
  ).filter(isDefined)
}

async function fetchForSerie(serie: Serie): Promise<FullSerie> {
  const seasonsNumbers = await getSeasonsNumbers(serie)
  const fullSeasons = (
    await Promise.all(
      seasonsNumbers.map(async (season) => {
        try {
          const timeRange = await getSeasonTimeRange(serie, season)
          return {
            seasonNumber: season,
            ...timeRange,
          }
        } catch (e) {
          console.log(
            `Discarding ${serie.id} ${serie.name} s${season}: ${e.message}`,
          )
          return null
        }
      }),
    )
  ).filter(isDefined)
  if (fullSeasons.length) {
    return {
      ...serie,
      seasons: fullSeasons,
    }
  }
  throw new Error(`Serie ${serie.id} ${serie.name} had no season`)
}
