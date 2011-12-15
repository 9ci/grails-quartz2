package grails.plugin.quartz2
import groovy.mock.interceptor.*
import grails.test.*
import org.quartz.*

class InvokeMethodJobTests extends GrailsUnitTestCase {
	def imj
	def jobExMock
	JobExecutionContext mockCtx = [
	           setResult: {z-> return null}
	       ] as JobExecutionContext
	
 	protected void setUp() {
        super.setUp()
		imj = new InvokeMethodJob()

    }

    protected void tearDown() {
        super.tearDown()
    }

    void testStatic() {
		imj.targetClass = FlyingObject
		imj.targetMethod = "staticGoo"
		imj.arguments = [true] as Object[]
		imj.execute(mockCtx)
		assert FlyingObject.ride
    }

	void testNormal() {
		def fo = new FlyingObject()
		imj.targetObject = fo
		imj.targetMethod = "ride"
		imj.arguments = [true] as Object[]
		def stub = new StubFor(HashMap) // you have to mock a class instead of an interface

		imj.execute(mockCtx)
		assert fo.thrill
    }

	void testNormalNoArg() {
		def fo = new FlyingObject()
		imj.targetObject = fo
		imj.targetMethod = "noArg"
		imj.execute(mockCtx)
		assert fo.thrill
   }
}

class FlyingObject{
	def thrill
	static def ride
	
	static void staticGoo(argx){
		ride = argx
	}
	boolean ride(boolean fuu){
		thrill = fuu
	}
	
	void noArg(){
		thrill = true
	}
}
