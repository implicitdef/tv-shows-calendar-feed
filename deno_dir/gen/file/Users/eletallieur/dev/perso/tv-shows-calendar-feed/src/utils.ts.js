export function firstAndLast(array) {
    if (array.length) {
        return [array[0], array[array.length - 1]];
    }
    throw new Error("Can't extract first and last of empty array");
}
export function isDefined(a) {
    return a !== undefined && a !== null;
}
// remove useless fields from API result
// unsafe
export function keepOnlyKeys(a, ...keysToKeep) {
    const res = {};
    keysToKeep.forEach((k) => {
        res[k] = a[k];
    });
    return res;
}
export function reverse(s) {
    return s.split('').reverse().join('');
}
//# sourceMappingURL=file:///Users/eletallieur/dev/perso/tv-shows-calendar-feed/deno_dir/gen/file/Users/eletallieur/dev/perso/tv-shows-calendar-feed/src/utils.ts.js.map