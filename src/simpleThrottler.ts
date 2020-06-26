// really simplistic throttling, not efficient
// because I can't find a decend lib usable from Deno
import { makeRunWithLimit } from './deps.ts'

export default class Throttler {
  runWithLimit: <A>(fn: () => Promise<A>) => Promise<A>
  constructor() {
    // max 3 promises will run at once
    this.runWithLimit = makeRunWithLimit(3).runWithLimit as any
  }
  schedule<A>(fn: () => Promise<A>): Promise<A> {
    return this.runWithLimit(fn)
  }
}
