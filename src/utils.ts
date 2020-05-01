export function firstAndLast<A>(array: A[]): A[] {
  if (array.length) {
    return [array[0], array[array.length - 1]]
  }
  throw new Error("Can't extract first and last of empty array")
}

export function isDefined<A>(a: A | undefined | null): a is A {
  return a !== undefined && a !== null
}
