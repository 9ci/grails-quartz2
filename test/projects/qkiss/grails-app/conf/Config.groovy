import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
import org.quartz.*
import qkiss.*

grails.config.locations = [ "file:app-${appName}-quartz.groovy"]
//                             "classpath:${appName}-config.groovy"

// log4j configuration
log4j = {
	root { info() }
    
	appenders {
        console name:'stdout', layout:pattern(conversionPattern: '%d{HH:mm:ss,SSS} [%t] %-5p %c %x - %m%n')
    }

    error  'org.codehaus.groovy.grails.web.servlet',  //  controllers
           'org.codehaus.groovy.grails.web.pages', //  GSP
           'org.codehaus.groovy.grails.web.sitemesh', //  layouts
           'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
           'org.codehaus.groovy.grails.web.mapping', // URL mapping
           'org.codehaus.groovy.grails.commons', // core / classloading
           'org.codehaus.groovy.grails.plugins', // plugins
           'org.codehaus.groovy.grails.orm.hibernate', // hibernate integration
           'org.springframework',
           'org.hibernate',
           'net.sf.ehcache.hibernate'

    warn   'org.mortbay.log'
    debug 	'qkiss'
}

grails.plugin.quartz2.jobSetup.job1 = { quartzScheduler, ctx ->
	def org = new Org(name:'skydive')
	Org.withTransaction{ //inside session
		org.save()
	}
	// define the job and tie it to our HelloJob class
	JobDetail job = newJob(HelloJob.class)
		.withIdentity("configTest")
		.usingJobData(new JobDataMap([jobDetailName:'mainConfig']) ) 
		.build()

	// Trigger the job to run now, and then every 40 seconds
	Trigger trigger = newTrigger()
		.withIdentity("configTestTrigger")
		.withSchedule(simpleSchedule()
			.withIntervalInSeconds(5)
			.repeatForever())  
		.startNow()          
		.build()

	// Tell quartz to schedule the job using our trigger
	quartzScheduler.scheduleJob(job, trigger);
}

