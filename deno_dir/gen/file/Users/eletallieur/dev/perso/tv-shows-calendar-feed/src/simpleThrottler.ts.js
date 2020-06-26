// really simplistic throttling, not efficient
// because I can't find a decend lib usable from Deno
import { makeRunWithLimit } from './deps.ts';
export default class Throttler {
    constructor() {
        // max 3 promises will run at once
        this.runWithLimit = makeRunWithLimit(3).runWithLimit;
    }
    schedule(fn) {
        return this.runWithLimit(fn);
    }
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/file/Users/eletallieur/dev/perso/tv-shows-calendar-feed/src/simpleThrottler.ts.js.map