import { fetchAll } from './fetchingService'
import { pushData } from './tvShowsCalendarClient'

async function start() {
  try {
    const data = await fetchAll()
    await pushData('local', data)
    await pushData('heroku', data)
    console.log('All done')
  } catch (err) {
    console.log(err)
  }
}

start()
