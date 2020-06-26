import { getBestSeriesAtPage, getSeasonsNumbers, getSeasonTimeRange, } from './themoviedbClient.ts';
import { isDefined } from './utils.ts';
export async function fetchAll(pagesToFetch = 100) {
    const pages = [...Array(pagesToFetch)].map((_, index) => index + 1);
    const series = (await Promise.all(pages.map((page) => fetchForPage(page)))).flat();
    console.log(`<< Success, got ${series.length}`);
    return series;
}
export async function fetchForPage(page) {
    const series = await getBestSeriesAtPage({ page });
    return (await Promise.all(series.map(async (serie) => {
        try {
            const fullSerie = await fetchForSerie(serie);
            console.log('<<<< Success for serie', fullSerie.id, fullSerie.name, `(${fullSerie.seasons.length} seasons)`);
            return fullSerie;
        }
        catch (e) {
            console.log(`Discarding serie ${serie.id} ${serie.name}: ${e.message}`);
            return null;
        }
    }))).filter(isDefined);
}
async function fetchForSerie(serie) {
    const seasonsNumbers = await getSeasonsNumbers(serie);
    const fullSeasons = (await Promise.all(seasonsNumbers
        // The API often adds a weird meaningless "season 0"
        .filter((_) => _ > 0)
        .map(async (season) => {
        try {
            const timeRange = await getSeasonTimeRange(serie, season);
            return {
                seasonNumber: season,
                ...timeRange,
            };
        }
        catch (e) {
            console.log(`Discarding ${serie.id} ${serie.name} s${season}: ${e.message}`);
            return null;
        }
    }))).filter(isDefined);
    if (fullSeasons.length) {
        return {
            ...serie,
            seasons: fullSeasons,
        };
    }
    throw new Error(`Serie ${serie.id} ${serie.name} had no season`);
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/file/Users/eletallieur/dev/perso/tv-shows-calendar-feed/src/fetchingService.ts.js.map