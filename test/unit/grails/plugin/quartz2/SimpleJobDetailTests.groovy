package grails.plugin.quartz2
import groovy.mock.interceptor.*
import grails.test.*
import org.quartz.*

class SimpleJobDetailTests extends GrailsUnitTestCase {

 	protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void testClone() {
		SimpleJobDetail sd  = new SimpleJobDetail("test",TestJob.class, [prop:'xyz'] )
		def sd2 = sd.clone()
		assert sd2==sd
    }

	void testFromMap() {
		def map = [name:"test",jobClass:TestJob.class, concurrent:false, jobData:[prop:'xyz'] ]
		def sd  = new SimpleJobDetail(map)
		assert sd.name == 'test'
		assert sd.jobClass == TestJob.class
		assert sd.concurrent == false
		//backed by !concurrent
		assert sd.isConcurrentExectionDisallowed() == true
		assert sd.jobDataMap.prop=='xyz'

    }

}

public class TestJob implements Job {
	static boolean ok = false
    public TestJob() {}

    void execute(JobExecutionContext jobCtx) {
		ok = true
    }

}
