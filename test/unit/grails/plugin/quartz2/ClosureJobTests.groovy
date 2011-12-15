package grails.plugin.quartz2
import groovy.mock.interceptor.*
import grails.test.*
import org.quartz.*
import static grails.plugin.quartz2.ClosureJob.createJob

class ClosureJobTests extends GrailsUnitTestCase {

	JobExecutionContext mockCtx = [
	           setResult: {z-> return null}
	       ] as JobExecutionContext
	
 	protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

    void test_justClosure() {
		def gotCalled
		def det = createJob{
			gotCalled = 'xxx'
		}
		//simulate quartx
		def job = det.jobClass.newInstance()
		job.groovyClosure = det.jobDataMap.groovyClosure
		job.execute(mockCtx)
		assert gotCalled == 'xxx'
    }

	void test_NameJobClosure() {
		def gotCalled
		def det = createJob("blah"){jobCtx->
			assert jobCtx
			gotCalled = 'xxx'
		}
		//simulate quartx
		def job = det.jobClass.newInstance()
		job.groovyClosure = det.jobDataMap.groovyClosure
		job.execute(mockCtx)
		assert gotCalled == 'xxx'
    }

	void test_NameJobClosure_withAppCtx() {
		def gotCalled
		def det = createJob("blah"){jobCtx,appCtx->
			assert jobCtx
			assert appCtx == 'something'
			gotCalled = 'xxx'
		}
		assert det.name == "blah"
		assert det.key.name == "blah"
		//simulate quartx
		def job = det.jobClass.newInstance()
		job.groovyClosure = det.jobDataMap.groovyClosure
		job.applicationContext = 'something'
		job.execute(mockCtx)
		assert gotCalled == 'xxx'
    }

	void test_contructorArgs_JobClosure() {
		def gotCalled
		def map = [name:"test",concurrent:false,durability:true,description:'dec test']
		def det = createJob(map){jobCtx,appCtx->
			assert jobCtx
			assert appCtx == 'something'
			gotCalled = 'xxx'
		}
		assert det.name == "test"
		assert det.key.name == "test"
		assert det.isDurable() 
		assert det.description == 'dec test'
		//simulate quartx
		def job = det.jobClass.newInstance()
		job.groovyClosure = det.jobDataMap.groovyClosure
		job.applicationContext = 'something'
		job.execute(mockCtx)
		assert gotCalled == 'xxx'
    }

}

