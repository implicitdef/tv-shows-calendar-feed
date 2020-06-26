import { firstAndLast, isDefined, keepOnlyKeys, reverse } from './utils.ts';
import { async } from './deps.ts';
import Throttler from './simpleThrottler.ts';
const API_KEY = reverse('2c11abad8a9845ff851767e6b8cff000');
const BASE_URL = 'https://api.themoviedb.org/3';
// In theory there is no rate limit on the API
// in practice if we go too fast the server doesn't answer anymore
const throttler = new Throttler();
async function withRetry(func, { nbTries = 0, timeoutBeforeRetry = 100 } = {}) {
    try {
        if (nbTries > 0) {
            console.log(`Retrying something for the ${nbTries}th time`);
        }
        return await func();
    }
    catch (e) {
        if (e.message.includes('timeout')) {
            await async.delay(timeoutBeforeRetry);
            return withRetry(func, {
                nbTries: nbTries + 1,
                // timeout increases exponentially, with a bit of randomness
                timeoutBeforeRetry: timeoutBeforeRetry * (2 + Math.random()),
            });
        }
        throw e;
    }
}
async function call(log, path, params = {}) {
    // We're unable to find a perfect rate for the limiter
    // we will still get timeouts from time to time
    // so we put a retry logic on top of it
    return withRetry(async () => {
        return throttler.schedule(async () => {
            const url = new URL(`${BASE_URL}${path}`);
            Object.entries({
                api_key: API_KEY,
                ...params,
            }).forEach(([k, v]) => url.searchParams.append(k, v));
            // on log tel quel ce qui nous a été
            console.log(log);
            // note : dans la version node.js avant on avait un timeout
            // il faudra peut-être en faire un avec AbortController
            const res = await fetch(url, {});
            if (!res.ok) {
                throw new Error(`HTTP response was not OK ${res.status} ${url}`);
            }
            const json = (await res.json());
            return json;
        });
    });
}
export async function getBestSeriesAtPage({ page = 1 } = {}) {
    const { results } = await call(`>> discover page ${page}`, '/discover/tv', {
        sort_by: 'popularity.desc',
        page,
    });
    return results.map((serie) => keepOnlyKeys(serie, 'id', 'name'));
}
export async function getSeasonsNumbers(serie) {
    const { seasons } = await call(`>>>> get seasons numbers of ${serie.id} ${serie.name}`, `/tv/${serie.id}`);
    return seasons.map((_) => _.season_number);
}
export async function getSeasonTimeRange(serie, season) {
    const { episodes } = await call(`>>>>>> get season time range ${serie.id} ${serie.name} S${season}`, `/tv/${serie.id}/season/${season}`);
    const airDates = episodes.map((_) => _.air_date).filter(isDefined);
    if (airDates.length) {
        const firstAndLastAirDate = firstAndLast(airDates);
        const [start, end] = firstAndLastAirDate.map((airDate) => {
            if (airDate.match(/^\d{4}-\d{2}-\d{2}$/)) {
                return new Date(`${airDate}T00:00:00Z`);
            }
            throw new Error(`Unexpected episode date for serie ${serie.name} ${serie.id} at season ${season}: ${airDate}`);
        });
        return { start, end };
    }
    throw new Error(`No valid episodes for serie ${serie.name} ${serie.id} at season ${season}`);
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/file/Users/eletallieur/dev/perso/tv-shows-calendar-feed/src/themoviedbClient.ts.js.map