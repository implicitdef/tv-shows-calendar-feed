import { fetchAll } from './fetchingService'
import { pushData } from './tvShowsCalendarClient'

async function start() {
  try {
    const data = await fetchAll(5)
    await pushData('local', data)
    console.log('All done')
  } catch (err) {
    console.log(err)
  }
}

start()
