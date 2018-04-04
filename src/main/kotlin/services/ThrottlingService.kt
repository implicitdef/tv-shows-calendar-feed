package services

import utils.CS
import java.time.Duration
import java.util.concurrent.DelayQueue
import java.util.concurrent.Delayed
import java.util.concurrent.TimeUnit

object ThrottlingService {

    private const val maxCallsInWindow = 40
    private val windowLength = Duration.ofSeconds(10)
    // Initiate the queue with N tokens, with 0 delay so they can be immediately taken
    private val initialTokens = (1..maxCallsInWindow).map { Token(Duration.ZERO) }
    private val registry = DelayQueue<Token>(initialTokens)

    class Token(private val initialDelay: Duration) : Delayed {

        private val availableAt = System.currentTimeMillis() + initialDelay.toMillis()

        override fun getDelay(unit: TimeUnit): Long =
            availableAt - System.currentTimeMillis()

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
            val newToken = Token(windowLength)
            registry.put(newToken)
        }
    }
}