import { Serie, TimeRange } from "./myTypes.ts";
import { firstAndLast, isDefined, keepOnlyKeys, reverse } from "./utils.ts";
import { async } from "./deps.ts";

type DiscoverEndpointResult = {
  results: { id: number; name: string }[];
};
type TvShowEndpointResult = {
  seasons: { season_number: number }[];
};
type SeasonEndpointResult = {
  episodes: { air_date: string | null }[];
};

const API_KEY = reverse("2c11abad8a9845ff851767e6b8cff000");
const BASE_URL = "https://api.themoviedb.org/3";

// TODO restorer rate limit

// In theory there is no rate limit on the API
// in practice if we go too fast the server doesn't answer anymore

async function withRetry<A>(
  func: () => Promise<A>,
  { nbTries = 0, timeoutBeforeRetry = 100 } = {},
): Promise<A> {
  try {
    if (nbTries > 0) {
      console.log(`Retrying something for the ${nbTries}th time`);
    }
    return await func();
  } catch (e) {
    if (e.message.includes("timeout")) {
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

async function call<R>(
  log: string,
  path: string,
  params: { [k: string]: string | number } = {},
): Promise<R> {
  // We're unable to find a perfect rate for the limiter
  // we will still get timeouts from time to time
  // so we put a retry logic on top of it
  return withRetry(async () => {
    // TODO ici on avait nesté le throttler à l'intérieur du retry
    const url = new URL(`${BASE_URL}${path}`);
    Object.entries({
      api_key: API_KEY,
      ...params,
    }).forEach(([k, v]) => url.searchParams.append(k, v));
    // TODO restore timeout 1000 with abort controller if needed
    const res = await fetch(url, {});
    if (!res.ok) {
      throw new Error(`HTTP response was not OK ${res.status} ${url}`);
    }
    const json = await res.json() as R;
    return json;
  });
}

export async function getBestSeriesAtPage({ page = 1 } = {}): Promise<Serie[]> {
  const { results } = await call<DiscoverEndpointResult>(
    `>> discover page ${page}`,
    "/discover/tv",
    {
      sort_by: "popularity.desc",
      page,
    },
  );
  return results.map((serie) => keepOnlyKeys(serie, "id", "name"));
}

export async function getSeasonsNumbers(serie: Serie): Promise<number[]> {
  console.log(">>>> get seasons numbers of ", serie.id, serie.name);
  const { seasons } = await call<TvShowEndpointResult>(
    `>>>> get seasons numbers of ${serie.id} ${serie.name}`,
    `/tv/${serie.id}`,
  );
  return seasons.map((_) => _.season_number);
}

export async function getSeasonTimeRange(
  serie: Serie,
  season: number,
): Promise<TimeRange> {
  const { episodes } = await call<SeasonEndpointResult>(
    `>>>>>> get season time range ${serie.id} ${serie.name} S${season}`,
    `/tv/${serie.id}/season/${season}`,
  );
  const airDates = episodes.map((_) => _.air_date).filter(isDefined);
  if (airDates.length) {
    const firstAndLastAirDate = firstAndLast(airDates);
    const [start, end] = firstAndLastAirDate.map((airDate) => {
      if (airDate.match(/^\d{4}-\d{2}-\d{2}$/)) {
        return new Date(`${airDate}T00:00:00Z`);
      }
      throw new Error(
        `Unexpected episode date for serie ${serie.name} ${serie.id} at season ${season}: ${airDate}`,
      );
    });
    return { start, end };
  }
  throw new Error(
    `No valid episodes for serie ${serie.name} ${serie.id} at season ${season}`,
  );
}
