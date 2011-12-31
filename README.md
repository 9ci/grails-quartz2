# A simple plugin for Quartz 2+ #

Uses the new [Quartz][] 2.1 framework from quartz-scheduler.org. The goal is to keep this as simple as possible while making it friendly for Groovy/Grails. This is (mostly) backward compatible with the original [Quartz plugin][] too so the concept of Job artifacts should work as well. 

## What this plugin adds to be friendly with Grails

* Its mostly backward compatible with the original [Quartz plugin][] so its allows scheduling jobs using job arifacts as well. See note below for areas where it is not compatible.
* Uses a factory to creates a single bean called quartzScheduler which is a standard Quartz [Scheduler][] and starts it. Its does not start it by default in test. You can inject and use the quartzScheduler bean like any normal Grails/Spring bean.
* All quartz settings can be done in Config.groovy, thus eliminating the need for a quartz.properties
* Sets up a PersistenceContextJobListener, makes it a bean and adds it to the scheduler. This wraps all the jobs to make sure they have a hibernate session bound to the thread or if using another (nosql) engine then this should work for other non-hibernate gorm engines too as it uses the "persistenceInterceptor" bean to init(). If you don't need gorm persistence in your job then you can avoid the overhead and turn it of by assigning a "gormSession:false" property in the the JobDataMap when setting up a [JobDetail][] or Trigger. Note: 
* adds a general InvokeMethodJob class that can be used to setup a [JobDetail][] to calls a service bean method or any static or local method on a passed in object
* support for assigning a builder closures in Config.groovy (or an externalized config) that will get called on application startup to setup your scheduler
* Adds a SimpleJobDetail - an implementation of the JobDetail that makes it easier to setDisallowConcurrentExecution with the need to put the annotaion on the Job class. Also makes it much easier to add JobDataMap properties by simply passing a map into the constructor
* ClosureJob - implements the Quartz [Job][] interface and is a utility class to allow you pass in configuration and a closure to be called when the Job executes.

## Additions and changes from [Quartz plugin][]

outside of the ability to setup quartz jobs through config as outlined above and below, the following changes were made

* Compatible with Grails 2x
* the configuration and quartz.properties can now all reside in the standard Config.groovy ( or externalized config.groovy) instead needing to be separate files
* uses the persistenceInterceptor spring bean to setup sessions instead of hibernate and sessionFactory allowing it to work better with other noSql plugins and the datasources plugins
* Removed any dependency on the Spring wrapper classes around quartz
* Removed deprecated volatility settings
* Triggers are no longer setup as spring beans
* Job artifact in the grails-app/jobs dir :
* The config no longer needs to be static and can be completely extracted into a Config
* a config property is injected into the Job artifact to make it easier to access it for setup. This injected property is actually a mergedConfig form the [plugin-config][]. This allows a plugin to setup a job with defaults and then allows the user of the plugin to override the settings much easier with more flexibility in the app (for example, change a trigger from a SimpleTrigger to a CronTrigger with more fine tuned control)

## Docs and Examples ##

clone this and look at the example test project under tests/projects/qkiss for examples on how its being used. Look at Config.groovy, the externalized app-qkiss-config.groovy on the root, and the integration tests. To get a schedule going in your app follow the examples in the [documentation and quick start][] and either set a grails.plugin.quartz2.jobSetup closure in config as in the example below or just inject the quartzScheduler([Scheduler][]) into BuildConfig and call quartzScheduler.scheduleJob once you have a JobDetail and Trigger setup.

### Config.groovy closure

You can externalize the config (see the grails docs on externalizing the config) and this allows us to configure and setup new jobs without recompiling the app. For example you could add something like grails.config.locations = [ "file:Quartz-config.groovy"] in your config.groovy and then follow the example below.
 
#### Example Quartz-config.groovy

	import static org.quartz.JobBuilder.*;
	import static org.quartz.SimpleScheduleBuilder.*;
	import static org.quartz.TriggerBuilder.*;
	import grails.plugin.quartz2.InvokeMethodJob

	grails.plugin.quartz2.autoStartup = true 

	org{
		quartz{
			//anything here will get merged into the quartz.properties so you don't need another file
			scheduler.instanceName = 'MyAppScheduler'
			threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
			threadPool.threadCount = 20
			threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
			jobStore.class = 'org.quartz.simpl.RAMJobStore'
		}
	}
	
	/you can drive the setup. just give them a unique key like "buyTheTicket" below.
	//the quartzScheduler bean and application context are passed to your closure
	grails.plugin.quartz2.jobSetup.buyTheTicket = { quartzScheduler, ctx ->
		//how it should look
		def jobDetail = ClosureJob.createJob { jobCtx , appCtx->
			appCtx.hunterService.takeTheRide(true)
		}
		
		def trigger1 = new SimpleTriggerImpl(name:"trig1", startTime:new Date(),repeatInterval:1000,repeatCount:-1)
		
		quartzScheduler.scheduleJob(jobDetail, trigger1)
	}
	
	grails.plugin.quartz2.jobSetup.buyTicket2 = { quartzScheduler, ctx  ->
		
		//example a service call using the InvokeMethodJob and quartz's new builder syntax
		def props = new JobDataMap([targetObject:ctx.hunterService,targetMethod:'takeTheRide',arguments:[true]])
		JobDetail jobDetail = newJob(InvokeMethodJob.class) //use the static helper newJob from org.quartz.JobBuilder
			.withIdentity("take the ride")
			.usingJobData(props)
			.build()
		
		Trigger trigger = newTrigger().withIdentity("hunter trigger")
			.withSchedule(
				simpleSchedule().withIntervalInSeconds(1).repeatForever()
			)  
			.startNow().build()
	
		quartzScheduler.scheduleJob(jobDetail, trigger)
	}
	
	
### SimpleJobDetail ###

SimpleJobDetail is an implementation of the Quartz [JobDetail][] interface. 
Its primary purpose was to make it easier to set make a Job or JobDetail single threaded and not need to 
name and jobClass are the only required fields

- **name** * - the jobKey name for this 
- **group** - the jobKey group name 
- **key** - set your own JobKey instead of constructing from a map
- **description** - description
- **jobClass** * - the Job class that will be instantiated and run via the exceute method 
- **jobDataMap** - a JobDataMap with any data that will be set to the JobExecutionContext
- **jobData** - a convenience setter to allow a normal Map to be set into the JobDataMap
- **concurrent** - setting this to false will make it so only 1 trigger at a time can run. isConcurrentExectionDisallowed will return true.
- **durability** - if this be stored even if no trigers
- **persistJobDataAfterExecution** 

#### examples
	
	def sd  = new SimpleJobDetail("test",TestJob.class, [prop:'xyz'] )
	...
	//gormSession:false will turn off the session init for gorm
	def map = [name:"test",jobClass:TestJob.class, concurrent:false, jobData:[fly:'free',gormSession:false] ]
	def sd  = new SimpleJobDetail(map)
	assert sd.isConcurrentExectionDisallowed() == false
	assert sd.jobDataMap.fly=='free'

### ClosureJob ###

ClosureJob implements the Quartz [Job][] interface and is a utility class to allow you pass in configuration and a closure to be called when the JOb executes.
the easiest way is to use the static method **createJob** which will return a **SimpleJobDetail**.
You can pass in a map of parameters to construct the SimpleJobDetail and then a closure that takes optional args for [JobExecutionContext][] and the spring application context .
To pass in values for the JobDataMap then just pass in a map of values to the jobData property such as 

#### examples

	//import the static as in previous examples
	def dataMap
	
	def jobDetail = ClosureJob.createJob(name:"test_quarts_builder",durability:true,concurrent:false){ jobCtx , appCtx->
		println "************* it ran ***********"
		//do something	
	}
	jobDetail.jobData = [gormSession:false]
	
	def trigger = TriggerBuilder.newTrigger().withIdentity("closureJobTrigger")
		.withSchedule(
			simpleSchedule()
			.withIntervalInMilliseconds(10)
			.withRepeatCount(2)
		).startNow().build()

	quartzScheduler.scheduleJob(jobDetail, trigger)

### InvokeMethodJob ###

InvokeMethodJob implements the Quartz [Job][] interface and facilitates calls to groovy's object.invokeMethod( targetMethod, args)  or the metaClass.invokeStaticMethod.
It is configured by setting up the JobDataMap with the following keys

- **targetObject** - the object to call the method on.
- **targetClass** - set this instead of targetObject if targetMethod is a static
- **targetMethod** - the method to call on the object, or static method to call on the targetClass
- **arguments** - list or array of arguments that you want passed to your method.

#### InvokeMethodJob examples

	import static org.quartz.JobBuilder.*
	import static org.quartz.SimpleScheduleBuilder.*
	import static org.quartz.TriggerBuilder.*
	import static org.quartz.DateBuilder.*
	import grails.plugin.quartz2.InvokeMethodJob
	...
	def jobDataMap = new JobDataMap([targetObject:ctx.hunterService,targetMethod:'takeTheRide',arguments:[true]])
	JobDetail job2 = newJob(InvokeMethodJob.class).withIdentity("take the ride")
		.usingJobData(jobDataMap)
		.build()
	
	Trigger trigger2 = newTrigger().withIdentity("hunter trigger2")
		.withSchedule(simpleSchedule().withIntervalInSeconds(4).repeatForever())  
		.startNow()
		.build()

	quartzScheduler.scheduleJob(job2, trigger2)
	
### Setting up your own job class ###

the plugin will set's the grailsAppication and appCtx (applicationContext) into "global" property into the schedules context.
Quartz, by default , uses the PropertySettingJobFactory which will attempt to inject the properties by keyName that exists in the schedule;s context, jobs context or triggers context into the Job when it creates it. So that mean if you setup your own Job and need the appCtx or grailsApplication just declare a field and it will get set.

Example:

	public class HelloJob implements Job {
	
		def grailsApplication

		/**
		 * Quartz requires a public empty constructor so that the
		 * scheduler can instantiate the class whenever it needs.
		 */
		public HelloJob() {}
		
		void execute(JobExecutionContext jobCtx) {
			def someConfigProp = grailsApplication.config.my.prop.here
			//or get a service with grailsApplication.mainContext.someService
			// Say Hello to the World and display the date/time
			println ("Hello from - ${jobCtx.jobDetail.key.name} - $someConfigProp" )
	    }

	}

### Job Artifact Example ###



Example:

	class ConfigTriggerJob{

		def concurrent = false	
	
		def getTriggers(){
			return config.grails.plugin.xyz.someTriggerConfig
		}
	
	    def execute() {
			//do something
	    }
	}

.. and setup the trigger builder just like it says in the docs here [Quartz plugin][].  example config:

	grails.plugin.xyz.someTriggerConfig = {
		//repeat every second
		simple repeatInterval: 1000l, repeatCount:1
	}


## Why we chose not to use or modify the quartz-plugin

* the changes in [Quartz][] 2 made for many incompatibilities with older 1.8. I think it will be difficult to have 1 plugin support both versions but it may be possible with some work. Spring 3.1 seemed to pull it off but with a considerable amount of ugly gyrations
* This plugin does not rely on the Spring support classes for quartz which the existing quartz-plugin does. Spring added support for [Quartz][] 2 in their upcoming 3.1 which will come with Grails 2. However we need and wanted Quartz 2 support now for our 1.3.x Grails apps
* When doing jobs on the fly at customer sites, we wanted something dirt simple, light weight and used the quartz api but got the job done to integrate with Grails

[documentation and quick start]: http://www.quartz-scheduler.org/documentation/quartz-2.1.x/quick-start
[Quartz]: http://www.quartz-scheduler.org
[Job]: http://www.quartz-scheduler.org/api/2.1.0/org/quartz/Job.html
[JobDetail]: http://www.quartz-scheduler.org/api/2.1.0/org/quartz/JobDetail.html
[JobExecutionContext]: http://www.quartz-scheduler.org/api/2.1.0/org/quartz/JobExecutionContext.html
[Scheduler]: http://www.quartz-scheduler.org/api/2.1.0/org/quartz/impl/StdScheduler.html
[Quartz plugin]: http://www.grails.org/plugin/quartz
[plugin-config]: http://grails.org/plugin/plugin-config