import { reverse } from 'esrever'
import axios from 'axios'
import { FullSerie } from './myTypes'

const targets = {
  local: {
    url: 'http://localhost:3333',
    apiKey: 'pushDataApiKey',
  },
  heroku: {
    url: 'https://tv-shows-calendar.herokuapp.com',
    apiKey: reverse('zL84m>:Y'),
  },
}

// La structure de donnée attendue par le serveur
type ExpectedDataFormat = {
  serie: {
    id: number
    name: string
  }
  seasons: {
    number: number
    time: {
      start: string
      end: string
    }
  }[]
}[]

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
  }))
}

export async function pushData(
  target: keyof typeof targets,
  data: FullSerie[],
) {
  const { url, apiKey } = targets[target]
  console.log('>> Posting data to ', url)
  await axios.post(`${url}/data`, JSON.stringify(restructureData(data)), {
    params: {
      key: apiKey,
    },
  })
}
