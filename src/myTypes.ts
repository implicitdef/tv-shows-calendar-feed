export type Serie = { id: number; name: string }
export type TimeRange = {
  start: Date
  end: Date
}
export type FullSeason = {
  seasonNumber: number
} & TimeRange
export type FullSerie = Serie & {
  seasons: FullSeason[]
}
