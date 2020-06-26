/**
 * Takes a number, setting the concurrency for the promise queue. Returns a set of functions to use the queue.
 */
export function makeRunWithLimit(concurrency) {
    if (concurrency < 1) {
        throw new Error("concurrency should be a positive number");
    }
    const queue = [];
    let activeCount = 0;
    async function run({ fn, resolve, reject }) {
        activeCount++;
        try {
            const result = await fn();
            resolve(result);
        }
        catch (error) {
            reject(error);
        }
        activeCount--;
        const mNextRunnable = queue.shift();
        if (typeof mNextRunnable !== "undefined") {
            run(mNextRunnable);
        }
    }
    async function enqueue(runnable) {
        if (activeCount < concurrency) {
            run(runnable);
        }
        else {
            queue.push(runnable);
        }
    }
    /**
     * Pass a thunk to this function that returns a promise.
     */
    async function runWithLimit(fn) {
        return new Promise((resolve, reject) => enqueue({ fn, resolve, reject }));
    }
    /**
     * Call to get the number of promises that are currently running.
     */
    function getActiveCount() {
        return activeCount;
    }
    /**
     * Call to check how many promises are still waiting to start execution.
     */
    function getPendingCount() {
        return queue.length;
    }
    return {
        runWithLimit,
        getActiveCount,
        getPendingCount,
    };
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/https/deno.land/x/run_with_limit@v0.1.0/mod.ts.js.map