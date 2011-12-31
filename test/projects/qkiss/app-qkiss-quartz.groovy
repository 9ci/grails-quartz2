import static org.quartz.JobBuilder.*;
import static org.quartz.SimpleScheduleBuilder.*;
import static org.quartz.CronScheduleBuilder.*;
import static org.quartz.CalendarIntervalScheduleBuilder.*;
import static org.quartz.TriggerBuilder.*;
import static org.quartz.DateBuilder.*;
import grails.plugin.quartz2.InvokeMethodJob
import grails.plugin.quartz2.SimpleJobDetail
import grails.plugin.quartz2.ClosureJob
import org.quartz.*
import org.quartz.impl.triggers.SimpleTriggerImpl
import qkiss.*

grails.plugin.quartz2.autoStartup = true

grails.plugin.quartz2.configTriggerJob = { 
	simple repeatInterval: 100l, repeatCount:1
}


org{
	quartz{
		scheduler.instanceName = 'TestAppScheduler'
		threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
		threadPool.threadCount = 5
		threadPool.threadPriority = 5
		threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
		jobStore.class = 'org.quartz.simpl.RAMJobStore'
		jobStore.misfireThreshold =60000
	}
}

grails.plugin.quartz2.jobSetup.job2 = { quartzScheduler, ctx  ->
	def org = new Org(name:'external')
	Org.withTransaction{
		org.save()
	}
	
	// define the job and tie it to our HelloJob class
	def jobDet = new SimpleJobDetail("hello from external",HelloFromExternalConfigJob.class, [orgId:org.id] )
	jobDet.concurrentExectionDisallowed = true
	assert jobDet.isConcurrentExectionDisallowed()
		
	Trigger trigger = newTrigger().withIdentity("HelloFromExternalConfigJobTrigger")
		.withSchedule(
			simpleSchedule()
			.withIntervalInSeconds(1)
			.withRepeatCount(1)
			.withMisfireHandlingInstructionNextWithExistingCount()
		)  
		.startNow()
		.build()
	
	quartzScheduler.scheduleJob(jobDet, trigger)
	
	
	
	//test the static call
	def dmap = new JobDataMap([targetClass:HunterService,targetMethod:'Buy_The_Ticket'])
	JobDetail job3 = newJob(InvokeMethodJob.class).withIdentity("buy the ticket")
		.usingJobData(dmap)
		.build()
	assert job3.isConcurrentExectionDisallowed() == false //I just wanted to verify that my metaClass overrid e above didn't stick
		
	Trigger trigger3 = newTrigger().withIdentity("hunter trigger1")
		.withSchedule(simpleSchedule().withIntervalInSeconds(4).repeatForever())  
		.startNow().build()
	
	quartzScheduler.scheduleJob(job3, trigger3)
	
	//test a service call
	def hunterService = ctx.hunterService
	def dmap2 = new JobDataMap([targetObject:hunterService,targetMethod:'takeTheRide',arguments:[true]])
	JobDetail job2 = newJob(InvokeMethodJob.class).withIdentity("take the ride")
		.usingJobData(dmap2).build()
		
	Trigger trigger2 = newTrigger().withIdentity("hunter trigger2")
		.withSchedule(simpleSchedule().withIntervalInSeconds(4).repeatForever())  
		.startNow().build()
	
	quartzScheduler.scheduleJob(job2, trigger2)
	
	def cj = ClosureJob.createJob { jobCtx , appCtx->
		appCtx.hunterService.takeTheRide(true)
		println "ClosureJob"
	}
	
	def ctrig = new SimpleTriggerImpl(name:"trig1", startTime:new Date(),repeatInterval:1000,repeatCount:-1)
	
	quartzScheduler.scheduleJob(cj, ctrig)
}



















/********Future builder? spec, not sure its worth it***********/
grails.plugin.quartz2.schedule.jobDetails=[
	cleverJob:[
		group:"someGroup", //(optional), sets the group int he JobKey
		description:"does something clever",
		jobClass:'grails.plugins.quartz2.SpringBeanMethodJob',
		jobDataMap:[beanName:'someService',beanMethod:"goBabyGo",methodParams:[1,2,3]],
	],
	anotherCleverJob:[
		group:'mainGroup',
		description:"does something clever",
		jobClosure:{ appCtx, jobCtx ->
			def org = ArDoc.get(1)
			if(arDoc) appCtx.outApiService.runOutApi(ardoc)
		}
	]
]//closes jobdetails list


grails.plugin.quartz2.schedule.triggers=[
	cleverJobTrigger:[
		triggerType:'cron', //can be cron,simple,custom
		startDelay:10000, //from here down its just property settings on the 
		cronExpression: '0/6 * 15 * * ?'
	]
]

