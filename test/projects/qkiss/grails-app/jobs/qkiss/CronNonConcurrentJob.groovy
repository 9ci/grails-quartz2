package qkiss
import java.util.concurrent.atomic.AtomicInteger
import org.quartz.*

class CronNonConcurrentJob{
	static AtomicInteger latch = new AtomicInteger(0)
	
	def concurrent = false
	
    static triggers = {
      cron cronExpression: '* * * * * ?', endTime: new Date(System.currentTimeMillis() + 1000) //, misfireInstruction:CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING 
    }

    def execute() {
		
		new Org(name:"CronNonConcurrentJob").save(flush:true)
		latch.incrementAndGet()
		//log.info "in CronNonConcurrentJob with count:${latch.incrementAndGet() }"
		sleep(2000) //will cause a misfire
    }
}
