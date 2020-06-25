import { FullSerie } from "./myTypes.ts";
import { reverse } from "./utils.ts";

const targets = {
  local: {
    url: "http://localhost:3333",
    apiKey: "pushDataApiKey",
  },
  heroku: {
    url: "https://tv-shows-calendar.herokuapp.com",
    apiKey: reverse("zL84m>:Y"),
  },
};

// La structure de donnée attendue par le serveur
type ExpectedDataFormat = {
  serie: {
    id: number;
    name: string;
  };
  seasons: {
    number: number;
    time: {
      start: string;
      end: string;
    };
  }[];
}[];

function restructureData(input: FullSerie[]): ExpectedDataFormat {
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

export async function pushData(
  target: keyof typeof targets,
  data: FullSerie[],
) {
  const { url, apiKey } = targets[target];
  console.log(">> Posting data to ", url);
  const url2 = new URL(`${url}/data`);
  url2.searchParams.append("key", apiKey);
  const res = await fetch(`${url}/data`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
    },
    body: JSON.stringify(restructureData(data)),
  });
  if (!res.ok) {
    throw new Error(`HTTP response was not OK ${res.status} ${url}`);
  }
}
