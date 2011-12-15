package qkiss

import org.junit.*
import grails.test.*
import grails.plugin.quartz2.*
import static grails.plugin.quartz2.ClosureJob.createJob
import org.quartz.*;
import org.quartz.impl.triggers.SimpleTriggerImpl
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class JobLookupTests extends GroovyTestCase {

    def quartzScheduler
	
	protected void setUp() {
        super.setUp()
		//how it should look
		def det = createJob(name:"inApi",durability:true,concurrent:false){ jobCtx , appCtx->
			sleep(5000)
	
		}
		assert det.concurrent == false


		Trigger trigger = newTrigger()
			.withSchedule( simpleSchedule().withIntervalInSeconds(1).withRepeatCount(2)
				.withMisfireHandlingInstructionNextWithExistingCount()
			)  
			.startNow().build()

		quartzScheduler.scheduleJob(det, trigger)

    }

    protected void tearDown() {
        super.tearDown()
    }

	
    void testMonitor() {
		def jlist = quartzScheduler.currentlyExecutingJobs
		while(jlist.size() >= 1){
			quartzScheduler.currentlyExecutingJobs.each{
				println "- $it.jobDetail.key.name | $it.fireTime | $it.fireInstanceId- ".toString()
				sleep(500)
			}
			jlist = quartzScheduler.currentlyExecutingJobs
		}
    }

	
}
