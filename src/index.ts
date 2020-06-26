import { fetchAll } from './fetchingService.ts'
import { pushData } from './tvShowsCalendarClient.ts'
async function start() {
  try {
    const data = await fetchAll()
    // await pushData("local", data);
    console.log('All done')
  } catch (err) {
    console.log(err)
  }
}

start()
