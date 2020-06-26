import { fetchAll } from './fetchingService.ts';
import { pushData } from './tvShowsCalendarClient.ts';
async function start() {
    try {
        const data = await fetchAll();
        await pushData('heroku', data);
        console.log('All done');
    }
    catch (err) {
        console.log(err);
    }
}
start();
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/file/Users/eletallieur/dev/perso/tv-shows-calendar-feed/src/index.ts.js.map