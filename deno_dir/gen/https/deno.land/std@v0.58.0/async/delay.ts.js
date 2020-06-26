// Copyright 2018-2020 the Deno authors. All rights reserved. MIT license.
/* Resolves after the given number of milliseconds. */
export function delay(ms) {
    return new Promise((res) => setTimeout(() => {
        res();
    }, ms));
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/https/deno.land/std@v0.58.0/async/delay.ts.js.map