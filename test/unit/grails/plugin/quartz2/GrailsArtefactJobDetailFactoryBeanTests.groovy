

package grails.plugin.quartz2

import grails.test.GrailsMock
import grails.test.mixin.*
import grails.test.mixin.support.*

import org.junit.*
import org.quartz.JobKey

/**
 * See the API for {@link grails.test.mixin.support.GrailsUnitTestMixin} for usage instructions
 */
@TestMixin(GrailsUnitTestMixin)
class GrailsArtefactJobDetailFactoryBeanTests {

    void setUp() {
        // Setup logic here
    }

    void tearDown() {
        // Tear down logic here
    }

    void testConcurrentJobCreation() {
		def jobClassMock = new GrailsMock(GrailsJobClass, true)
		jobClassMock.demand.getJobKey(1) { return new JobKey("111") }
		jobClassMock.demand.isConcurrent(2) { return true }
		jobClassMock.demand.getFullName(1) { return "myfullname" }
		jobClassMock.demand.isSessionRequired(1) { return true }
		jobClassMock.demand.isGormSession(1) { return true }
		jobClassMock.demand.getDurability(1) { return true }
		jobClassMock.demand.getRequestsRecovery(1) { return true }
		
		def factory = new GrailsArtefactJobDetailFactoryBean()
		factory.grailsJobClass = jobClassMock.createMock()
		factory.afterPropertiesSet()
		
		assert factory.getObject().getJobClass() == GrailsArtefactJob.class
    }
	
    void testNonConcurrentJobCreation() {
		def jobClassMock = new GrailsMock(GrailsJobClass, true)
		jobClassMock.demand.getJobKey(1) { return new JobKey("111") }
		jobClassMock.demand.isConcurrent(2) { return false }
		jobClassMock.demand.getFullName(1) { return "myfullname" }
		jobClassMock.demand.isSessionRequired(1) { return true }
		jobClassMock.demand.isGormSession(1) { return true }
		jobClassMock.demand.getDurability(1) { return true }
		jobClassMock.demand.getRequestsRecovery(1) { return true }
		
		def factory = new GrailsArtefactJobDetailFactoryBean()
		factory.grailsJobClass = jobClassMock.createMock()
		factory.afterPropertiesSet()
		
		assert factory.getObject().getJobClass() == GrailsArtefactNonConcurrentJob.class
    }
}
