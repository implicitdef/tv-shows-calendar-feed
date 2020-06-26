/** Creates a Promise with the `reject` and `resolve` functions
 * placed as methods on the promise object itself. It allows you to do:
 *
 *     const p = deferred<number>();
 *     // ...
 *     p.resolve(42);
 */
export function deferred() {
    let methods;
    const promise = new Promise((resolve, reject) => {
        methods = { resolve, reject };
    });
    return Object.assign(promise, methods);
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/https/deno.land/std@v0.58.0/async/deferred.ts.js.map