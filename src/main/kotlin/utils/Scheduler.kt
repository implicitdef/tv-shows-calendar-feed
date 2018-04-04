package utils
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.support.CronTrigger
import utils.Utils.log

object Scheduler {

    // every day at 03:00
    val cronExpression = "0 0 3 * * *"

    fun schedule(lambda: () -> Unit) {
        log("Scheduling a task with cron expression $cronExpression")
        val scheduler = ThreadPoolTaskScheduler()
        scheduler.initialize()
        scheduler.schedule(object: Runnable {
            override fun run() {
                log("Running scheduled task...")
                lambda()
            }
        }, CronTrigger(cronExpression))
    }

}