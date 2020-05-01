import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { fetchAll } from './fetchingService'

async function start() {
  const breakingBad = { id: 1396, name: 'Breaking Bad' }
  const res = await fetchAll(2)
  console.log(res)
}

start()
