package qkiss

import grails.test.*
import grails.plugin.quartz2.*
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import org.quartz.*;

class ArtifactTests extends GroovyTestCase {
	
	def quartzScheduler
	def grailsApplication
	
    protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }


	void testSimpleJob() {
		sleep(1000) 
		 assert SimpleJob.latch.intValue() == 5
		 def ol = Org.findAllByName('SimpleJob')
		 assert ol.size()==5
		def ctx = grailsApplication.mainContext
        assertTrue "Bean 'SimpleJob' is not registered in application context", ctx.containsBean('qkiss.SimpleJob')
        assertTrue "Bean 'SimpleJobClass' is not registered in application context", ctx.containsBean('qkiss.SimpleJobClass')
        assertTrue "Bean 'SimpleJobJobDetail' is not registered in application context", ctx.containsBean('qkiss.SimpleJobDetail')
        //assertTrue "Bean 'SimpleJobTrigger' is not registered in application context", ctx.containsBean('qkiss.SimpleJob0Trigger')
		// def trig = quartzScheduler.getTrigger(new TriggerKey('qkiss.SimpleJob0',Scheduler.DEFAULT_GROUP))
		// assert trig instanceof org.quartz.impl.triggers.SimpleTriggerImpl
    }

	void testCronNonConcurrentJob() {
		sleep(2500) 
		//assert CronNonConcurrentJob.latch.intValue() == 1
		def ol = Org.findAllByName('CronNonConcurrentJob')
		assert ol.size()==2
		assert CronNonConcurrentJob.latch.intValue() == 2
    }

	void testConfigTriggerJob() {
		sleep(1000) 
		assert ConfigTriggerJob.latch.intValue() == 2
		def ol = Org.findAllByName('ConfigTriggerJob')
		assert ol.size()==2
	}

}
