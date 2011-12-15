import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
import org.quartz.JobDetail
import org.quartz.Trigger

class BootStrap {
	def grailsApplication
	def quartzScheduler 
    
	def init = { servletContext ->
		// define the job and tie it to our HelloJob class
/*		JobDetail job = newJob(HelloJob.class)
			.withIdentity("JobTest")
			.usingJobData("jobDetailName",'BootStrap')
			.build()

		// Trigger the job to run now, and then every 40 seconds
		Trigger trigger = newTrigger()
			.withIdentity("myTrigger")
			.withSchedule(simpleSchedule()
				.withIntervalInSeconds(4)
				.repeatForever())  
			.startNow()          
			.build()

		// Tell quartz to schedule the job using our trigger
		quartzScheduler.scheduleJob(job, trigger);*/
		
		
    }
    def destroy = {
	
    }
}
