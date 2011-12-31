package qkiss
import java.util.concurrent.atomic.AtomicInteger

class CronJob{
	static AtomicInteger latch = new AtomicInteger(0)
	
    //static triggers = {
    //  simple repeatInterval: 100l, repeatCount:4 
    //}

    def execute() {
		new Org(name:"CronJob").save()
		log.info "in CronJob with count:${latch.incrementAndGet() }"
    }
}
