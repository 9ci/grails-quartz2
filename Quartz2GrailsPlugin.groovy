/*
 * Copyright (c) 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import grails.plugin.quartz2.*
import org.quartz.*
import org.springframework.context.ApplicationContext
import org.springframework.beans.factory.config.MethodInvokingFactoryBean

class Quartz2GrailsPlugin {
    // the plugin version
    def version = "0.2.3"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3 > *"

	def pluginExcludes = [
		"grails-app/views/**/*",
		"grails-app/controllers/**/*",
		"web-app/**/*"
	]

    // TODO Fill in these fields
    def author = "Joshua Burentt"
    def authorEmail = "Joshua@greenbill.com"
    def title = "Quartz 2.x Scheduler"
    def description = '''\\
Uses the new Quartz 2 framework from quartz-scheduler.org. 
The goal is to keep it as simple as possible while making it friendly for Groovy/Grails.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/quartz2"

    def watchedResources = [
            "file:./grails-app/jobs/**/*Job.groovy",
            "file:./plugins/*/grails-app/jobs/**/*Job.groovy"
    ]

    def artefacts = [new JobArtefactHandler()]

	def loadAfter = ['hibernate']

    def doWithSpring = {
		def mcfg = application.mergedConfig
		MergedConfigHolder.config = application.mergedConfig
		
		def quartzProps = loadQuartzConfig(mcfg)
		
		application.jobClasses.each {jobClass ->
            configureJobBeans.delegate = delegate
            configureJobBeans(jobClass)
        }

		// persistenceContextJobListener(PersistenceContextJobListener){
		// 	persistenceInterceptor = ref("persistenceInterceptor")
		// }
		
    	jobErrorLoggerListener(JobErrorLoggerListener)
		quartzJobFactory(GrailsJobFactory)
		
        quartzScheduler(QuartzFactoryBean) {
			grailsApplication = ref('grailsApplication')
            quartzProperties = quartzProps
			jobFactory = quartzJobFactory
			// delay scheduler startup to after-bootstrap stage
			//autoStartup = false
            // delay scheduler startup to after-bootstrap stage
            autoStartup = mcfg.grails.plugin.quartz2.autoStartup
            globalJobListeners = [ref('jobErrorLoggerListener')]//,ref('persistenceContextJobListener')]
			if (mcfg.grails.plugin.quartz2.jdbcStore) {
                dataSource = ref('dataSource')
                transactionManager = ref('transactionManager')
            }
        }

		/* for future reloading
		scheduledExecutorForReloading(org.springframework.scheduling.concurrent.ScheduledExecutorFactoryBean){
			continueScheduledExecutionAfterException=true
	        waitForTasksToCompleteOnShutdown=true
	        scheduledExecutorTasks = [ref('scheduledExecutorTaskForReloading')]
		}
		scheduledExecutorTaskForReloading(org.springframework.scheduling.concurrent.ScheduledExecutorTask){
			runnable = ref('configReloadingTask')
		    delay=5000 //5 seconds
		    period=5000 //5 seconds
		}
		
		configReloadingTask(ConfigReloadingTask)
		*/
    }

    def doWithDynamicMethods = {ctx ->
        def random = new Random()
        Scheduler quartzScheduler = ctx.getBean('quartzScheduler')
        application.jobClasses.each {GrailsJobClass tc ->
            def mc = tc.metaClass
			//println "** doWithDynamicMethods adding methods to tc.getFullName()"
            def jobName = tc.getFullName()
            def jobGroup = tc.getGroup()
			def jobKey = tc.jobKey

			//tc.clazz.metaClass.static.getGrailsApplication
			tc.clazz.metaClass.getConfig = { ->
				return application.mergedConfig
			}
			
            mc.'static'.schedule = { String cronExpression, Map params = null ->
                //Trigger trigger = new CronTriggerImpl(generateTriggerName(), Constants.DEFAULT_TRIGGERS_GROUP, jobName, jobGroup, cronExpression)
				def trigger = TriggerHelper.cronTrigger(jobKey,cronExpression,params)
                quartzScheduler.scheduleJob(trigger)
            }
            mc.'static'.schedule = {Long interval, Integer repeatCount = SimpleTrigger.REPEAT_INDEFINITELY, Map params = null ->
                //Trigger trigger = new SimpleTriggerImpl(generateTriggerName(), Constants.DEFAULT_TRIGGERS_GROUP, jobName, jobGroup, new Date(), null, repeatCount, interval)
                def trigger = TriggerHelper.simpleTrigger(jobKey,new Date(),repeatCount,interval,params)
                quartzScheduler.scheduleJob(trigger)
            }
            mc.'static'.schedule = {Date scheduleDate ->
                //Trigger trigger = new SimpleTrigger(generateTriggerName(), Constants.DEFAULT_TRIGGERS_GROUP, jobName, jobGroup, scheduleDate, null, 0, 0)
                def trigger = TriggerHelper.simpleTrigger(jobKey,scheduleDate,0,0,null)
				quartzScheduler.scheduleJob(trigger)
            }
            mc.'static'.schedule = {Date scheduleDate, Map params ->
                //Trigger trigger = new SimpleTrigger(generateTriggerName(), Constants.DEFAULT_TRIGGERS_GROUP, jobName, jobGroup, scheduleDate, null, 0, 0)
                def trigger = TriggerHelper.simpleTrigger(jobKey,scheduleDate,0,0,params)
				quartzScheduler.scheduleJob(trigger)
            }
            mc.'static'.schedule = {Trigger trigger ->
                trigger.jobKey = jobKey
                quartzScheduler.scheduleJob(trigger)
            }
            mc.'static'.triggerNow = { Map params = null ->
                quartzScheduler.triggerJob(jobKey, params ? new JobDataMap(params) : null)
            }
            mc.'static'.removeJob = {
                quartzScheduler.deleteJob(jobKey)
            }

            mc.'static'.reschedule = {Trigger trigger ->
				trigger.jobKey = jobKey
                quartzScheduler.rescheduleJob(trigger.key, trigger)
            }

            mc.'static'.unschedule = {String triggerName, String triggerGroup = Constants.DEFAULT_TRIGGERS_GROUP ->
                quartzScheduler.unscheduleJob(new TriggerKey(triggerName,triggerGroup))
            }
			
        }
    }


    def doWithApplicationContext = { ctx ->
		def scheduler = ctx.getBean("quartzScheduler")
        if (scheduler) {
			//do this here so that 
			if(ctx.persistenceInterceptor){
				def listener = new PersistenceContextJobListener()
				listener.persistenceInterceptor = ctx.persistenceInterceptor
				scheduler.listenerManager.addJobListener(listener)
			}

	        application.jobClasses.each {jobClass ->
				//println "** doWithApplicationContext adding methods to jobClass.getFullName()"
	            scheduleJob.delegate = delegate
	            scheduleJob(jobClass, ctx, scheduler)
	        }
	
			if(application.mergedConfig.grails.plugin.quartz2.autoStartup){
				def builders = application.mergedConfig.grails.plugin.quartz2.jobSetup.flatten()
				if(builders?.keySet()){
					builders.each{key,clos->
						clos(scheduler,ctx)
					}
				}
			}
		}
    }

    def onChange = {event ->
        if (application.isArtefactOfType(JobArtefactHandler.TYPE, event.source)) {
            log.debug("Job ${event.source} changed. Reloading...")
            def context = event.ctx
            def scheduler = context?.getBean("quartzScheduler")
            // get quartz scheduler
            if (context && scheduler) {
                // if job already exists, delete it from scheduler
                def jobClass = application.getJobClass(event.source?.name)
                if (jobClass) {
                    scheduler.deleteJob(jobClass.jobKey)
                    log.debug("Job ${jobClass.fullName} deleted from the scheduler")
                }

                // add job artefact to application
                jobClass = application.addArtefact(JobArtefactHandler.TYPE, event.source)

                // configure and register job beans
                def fullName = jobClass.fullName
                def beans = beans {
                    configureJobBeans.delegate = delegate
                    configureJobBeans(jobClass)
                }

                context.registerBeanDefinition("${fullName}Class", beans.getBeanDefinition("${fullName}Class"))
                context.registerBeanDefinition("${fullName}", beans.getBeanDefinition("${fullName}"))
                context.registerBeanDefinition("${fullName}Detail", beans.getBeanDefinition("${fullName}Detail"))

                // jobClass.triggers.each {name, trigger ->
                //     event.ctx.registerBeanDefinition("${name}Trigger", beans.getBeanDefinition("${name}Trigger"))
                // }

                scheduleJob(jobClass, event.ctx, scheduler)
            } else {
                log.error("Application context or Quartz Scheduler not found. Can't reload Quartz plugin.")
            }
        }
    }

    def scheduleJob = {GrailsJobClass jobClass,  ctx, scheduler ->

		def fullName = jobClass.fullName
		// add job to scheduler, and associate triggers with it
		def jobDetail = ctx.getBean("${fullName}Detail")
		scheduler.addJob(jobDetail, true)
		jobClass.triggers.each {key, trigger ->
			//println("Scheduling $fullName with trigger $key: ${trigger} with name:${trigger.triggerAttributes.name}")
			def tkey = new TriggerKey(trigger.triggerAttributes.name,trigger.triggerAttributes.group) 
			def trigInstance = TriggersBuilder.createTrigger(trigger,jobDetail.key)
			if (scheduler.getTrigger(tkey)) {
				scheduler.rescheduleJob(tkey, trigInstance)
			} else {
				scheduler.scheduleJob(trigInstance)
			}
		}
    }

    def configureJobBeans = {GrailsJobClass jobClass ->        
		def fullName = jobClass.fullName
		
        "${fullName}Class"(MethodInvokingFactoryBean) {
            targetObject = ref("grailsApplication", true)
            targetMethod = "getArtefact"
            arguments = [JobArtefactHandler.TYPE, jobClass.fullName]
        }

        "${fullName}"(ref("${fullName}Class")) {bean ->
            bean.factoryMethod = "newInstance"
            bean.autowire = "byName"
            bean.scope = "prototype"
        }

        "${fullName}Detail"(GrailsArtefactJobDetailFactoryBean) {
            grailsJobClass = jobClass
        }

        // registering triggers
        // jobClass.triggers.each {name, trigger ->
        //     "${name}Trigger"(trigger.clazz) {
        //         jobDetail = ref("${fullName}Detail")
        //         trigger.properties.findAll {it.key != 'clazz'}.each {
        //             delegate["${it.key}"] = it.value
        //         }
        //     }
        // }
    }
	
	Properties loadQuartzConfig(config) {
        def properties = new Properties()
        if (config.org.containsKey('quartz')) {
            properties << config.org.quartz.toProperties('org.quartz')
        }

        //config.quartz._properties = properties

        return properties
    }

}
