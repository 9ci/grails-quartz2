package qkiss

import grails.test.*
import grails.plugin.quartz2.*
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import org.quartz.*;

class SpringBeanJobTests extends GroovyTestCase {
	
	def quartzScheduler
	
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }


	void testCreateJobConstructor() {

		def jobMap = [name:'sbjob',bean:'springBeanTestService',method:'createOrg',arguments:'SpringBeanJobTests']
		def sbjob = SpringBeanJob.createJob(jobMap)
		
		Trigger trigger = newTrigger().withIdentity("sbjobTrigger")
			.withSchedule(
				simpleSchedule().withIntervalInMilliseconds(10).withRepeatCount(4) //it does one more than this
			)
			.startNow().build()

		quartzScheduler.scheduleJob(sbjob, trigger)
		
		sleep(500) //sleep a 1/2 sec to let it finish
		def ol = Org.findAllByName('SpringBeanJobTests')
		assert ol.size()==5
    }
}
