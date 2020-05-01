import {
  getBestSeriesAtPage,
  getSeasonsNumbers,
  getSeasonTimeRange,
} from './themoviedbClient'
import { fetchAll, fetchForPage } from './fetchingService'

async function start() {
  try {
    const mm = await Promise.all([
      fetchForPage(1),
      fetchForPage(2),
      fetchForPage(3),
    ])
    console.log('OK', mm)
  } catch (err) {
    console.log(err)
  }
}

start()
