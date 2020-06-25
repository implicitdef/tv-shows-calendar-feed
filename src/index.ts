import { async } from './deps.ts'

console.log('hello')
;(async () => {
  await async.delay(3000)
  console.log('yo')
})()
