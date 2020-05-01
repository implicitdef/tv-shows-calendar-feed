export function firstAndLast<A>(array: A[]): A[] {
  if (array.length) {
    return [array[0], array[array.length - 1]]
  }
  throw new Error("Can't extract first and last of empty array")
}

export function isDefined<A>(a: A | undefined | null): a is A {
  return a !== undefined && a !== null
}

// remove useless fields from API result
// unsafe
export function keepOnlyKeys<A extends {}>(a: A, ...keysToKeep: string[]): A {
  const res: any = {}
  keysToKeep.forEach((k) => {
    res[k] = (a as any)[k]
  })
  return res
}

export function timeoutPromise(delay: number): Promise<void> {
  return new Promise((resolve) => setTimeout(resolve, delay))
}
