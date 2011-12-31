package qkiss

import grails.test.*

class BuilderConfigTests extends GroovyTestCase {

    def quartzScheduler

	protected void setUp() {
        super.setUp()
    }

    protected void tearDown() {
        super.tearDown()
    }

	
    void testHelloRan() {
		sleep(3000)
		assert HelloJob.ok
		assert HelloFromExternalConfigJob.ok
		assert HelloFromExternalConfigJob.firedCount == 2
    }

	void testHunter() {
		sleep(1000)
		assert HunterService.boughtTheTicket
		assert HunterService.tookTheRide
		assert HunterService.boughtTheTicketAndTookTheRide
    }

	
}
