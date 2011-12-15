package qkiss

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import qkiss.Org

public class HelloJob implements Job {
    private static Logger log = LoggerFactory.getLogger(HelloJob.class);
	
	def grailsApplication
	//def org
	static boolean ok = false
    /**
     * Quartz requires a public empty constructor so that the
     * scheduler can instantiate the class whenever it needs.
     */
    public HelloJob() {}

    void execute(JobExecutionContext jobCtx) {

        // Say Hello to the World and display the date/time
        log.info("Hello from - ${jobCtx.jobDetail.key.name} }" )
		//throw JobExecutionException 
		//log.info("MergedMap ${jobCtx.mergedJobDataMap}" )
		//make sure persitance is working ok
		def o = Org.get(1)
		if(o) log.info("got skydive")
		new Org(name:"bill").save(failOnError:true)
		ok = true
    }

}
