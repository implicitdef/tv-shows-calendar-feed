import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { fetchAll } from './fetchingService'

async function start() {
  try {
    await fetchAll(1)
  } catch (err) {
    console.log(err)
  }
}

start()
