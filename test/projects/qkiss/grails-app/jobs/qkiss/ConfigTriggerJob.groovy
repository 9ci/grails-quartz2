package qkiss
import java.util.concurrent.atomic.AtomicInteger

class ConfigTriggerJob{
	static AtomicInteger latch = new AtomicInteger(0)
	
	def concurrent = false	
	
	def getTriggers(){
		return config.grails.plugin.quartz2.configTriggerJob
	}
	
    def execute() {
		
		new Org(name:"ConfigTriggerJob").save()
		log.info "in ConfigTriggerJob with count:${latch.incrementAndGet() }"
    }
}
