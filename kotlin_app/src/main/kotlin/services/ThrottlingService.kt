package services

import utils.CS
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

object ThrottlingService {

    // TODO doesn't work, reimplement with https://github.com/bbeck/token-bucket maybe


    private const val maxCallsInWindow = 3
    private val windowLengthSeconds = 1
    // Initiate the queue with N tokens, with 0 delay so they can be immediately taken
    private val initialTokens = (1..maxCallsInWindow).map {Token(0) }
    private val registry = DelayQueue<Token>(initialTokens)

    class Token(val delayInSeconds: Long): Delayed {
        override fun getDelay(unit: TimeUnit): Long =
           unit.convert(delayInSeconds, TimeUnit.SECONDS)
        override fun compareTo(other: Delayed): Int {
            val a = getDelay(TimeUnit.NANOSECONDS)
            val b = other.getDelay(TimeUnit.NANOSECONDS)
            if (a == b) return 0
            if (a < b) return -1
            return -1
        }
    }

    fun <Output> withThrottling(func: () -> CS<Output>): CS<Output> {
        // take a token
        // will block if there's no token available
        registry.take()
        return func().whenComplete { _, _ ->
            // Puts a token back, but it will be available only in some time
            registry.put(Token(windowLengthSeconds.toLong()))
        }
    }


}