import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { fetchAll, fetchForPage } from './fetchingService'

async function start() {
  try {
    const mm = await fetchAll(3)
    console.log('OK', mm)
  } catch (err) {
    console.log(err)
  }
}

start()
