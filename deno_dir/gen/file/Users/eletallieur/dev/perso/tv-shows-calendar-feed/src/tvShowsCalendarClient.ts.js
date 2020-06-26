import { reverse } from './utils.ts';
const targets = {
    local: {
        url: 'http://localhost:3333',
        apiKey: 'pushDataApiKey',
    },
    heroku: {
        url: 'https://tv-shows-calendar.herokuapp.com',
        apiKey: reverse('zL84m>:Y'),
    },
};
function restructureData(input) {
    return input.map(({ id, name, seasons }) => ({
        serie: { id, name },
        seasons: seasons.map(({ seasonNumber, start, end }) => ({
            number: seasonNumber,
            time: {
                start: start.toISOString(),
                end: end.toISOString(),
            },
        })),
    }));
}
export async function pushData(target, data) {
    const { url, apiKey } = targets[target];
    console.log('>> Posting data to ', url);
    const url2 = new URL(`${url}/data`);
    url2.searchParams.append('key', apiKey);
    const res = await fetch(url2, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(restructureData(data)),
    });
    if (!res.ok) {
        throw new Error(`HTTP response was not OK ${res.status} ${url}`);
    }
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/file/Users/eletallieur/dev/perso/tv-shows-calendar-feed/src/tvShowsCalendarClient.ts.js.map