import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { fetchAll, fetchForPage } from './fetchingService'

async function start() {
  try {
    const mm = await fetchAll()
    console.log('OK', mm)
  } catch (err) {
    console.log(err)
  }
}

start()
