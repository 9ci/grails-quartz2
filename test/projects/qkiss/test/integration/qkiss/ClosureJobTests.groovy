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

class ClosureJobTests extends GroovyTestCase {
	private static Logger logger = LoggerFactory.getLogger(ClosureJobTests.class);
    def quartzScheduler

	@BeforeClass
	static void BeforeClass() {
		
	}
	
	protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	
    void testBasic() {
		int latch = 0
		//how it should look
		def det = ClosureJob.createJob("closureJob"){ jobCtx , appCtx->
			println "************* it ran ***********"
			latch ++	
		}
		
		def trig = new SimpleTriggerImpl(name:"closureJobTrig", startTime:new Date(),repeatInterval:100	,repeatCount:2)
		quartzScheduler.scheduleJob(det, trig)
		
		sleep(1000) //sleep for 3 seconds
		assert  latch == 3 
		
    }

	void test_quarts_builder() {
		int latch = 0
		//how it should look
		def det = createJob(name:"test_quarts_builder",durability:true){ jobCtx , appCtx->
			println "************* it ran ***********"
			latch ++	
		}

		Trigger trigger = newTrigger().withIdentity("closureJobTrigger")
			.withSchedule(
				simpleSchedule().withIntervalInMilliseconds(10).withRepeatCount(2)
			).startNow()
			.build()

		quartzScheduler.scheduleJob(det, trigger)
		
		sleep(1000) //sleep for 3 seconds
		assert  latch == 3
		//should be there since we have durable on
		assert quartzScheduler.checkExists(JobKey.jobKey("test_quarts_builder"))
		
    }

	

	
}
