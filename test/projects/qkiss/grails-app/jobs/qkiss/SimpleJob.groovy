package qkiss
import java.util.concurrent.atomic.AtomicInteger

class SimpleJob{
	static AtomicInteger latch = new AtomicInteger(0)
	def grailsApplication
	
    def triggers = {
      simple repeatInterval: 100l, repeatCount:4 
    }

    def execute() {
		//println "grailsApplication config ${grailsApplication.config.grails.plugin.quartz2}"
		new Org(name:"SimpleJob").save()
		log.info "in SimpleJob with count:${latch.incrementAndGet() }"
    }
}
