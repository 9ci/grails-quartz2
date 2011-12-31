package qkiss
import java.util.concurrent.atomic.AtomicInteger

class SpringBeanTestService{

    static transactional = true

	static AtomicInteger latch = new AtomicInteger(0)


    def createOrg(String name) {
		new Org(name:name).save()
    }

}
