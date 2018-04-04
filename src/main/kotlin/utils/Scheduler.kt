package utils
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger

object Scheduler {

    // every day at 03:00
    val cronExpression = "0 0 3 * * *"

    fun schedule(lambda: () -> Unit) {
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.initialize()
        scheduler.schedule(object: Runnable {
            override fun run() {
                lambda()
            }
        }, CronTrigger(cronExpression))
    }

}