import grails.plugin.quartz2.*

class Quartz2GrailsPlugin {
    // the plugin version
    def version = "0.2.1.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "1.3.1 > *"
    // the other plugins this plugin depends on
    def dependsOn = [:]
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
            "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def author = "Joshua Burentt"
    def authorEmail = "Joshua@greenbill.com"
    def title = "Quartz 2.1 Scheduler"
    def description = '''\\
Uses the new Quartz 2.1 framework from quartz-scheduler.org. 
The goal is to keep it as simple as possible while making it friendly for Groovy/Grails.
'''

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/quartz2"

    def doWithWebDescriptor = { xml ->
        // TODO Implement additions to web.xml (optional), this event occurs before 
    }

    def doWithSpring = {
		def mcfg = application.mergedConfig
		def quartzProps = loadQuartzConfig(mcfg)
		
		persistenceContextJobListener(PersistenceContextJobListener){
			persistenceInterceptor = ref("persistenceInterceptor")
		}
		
    	jobErrorLoggerListener(JobErrorLoggerListener)

        quartzScheduler(QuartzFactoryBean) {
			grailsApplication = ref('grailsApplication')
            quartzProperties = quartzProps
            // delay scheduler startup to after-bootstrap stage
            autoStartup = mcfg.grails.plugins.quartz2.autoStartup
            globalJobListeners = [ref('jobErrorLoggerListener'),ref('persistenceContextJobListener')]
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

    def doWithDynamicMethods = { ctx ->
        // TODO Implement registering dynamic methods to classes (optional)
    }

    def doWithApplicationContext = { ctx ->
		if(application.mergedConfig.grails.plugins.quartz2.autoStartup){
			def builders = application.mergedConfig.grails.plugin.quartz2.jobSetup.flatten()
			if(builders?.keySet()){
				builders.each{key,clos->
					clos(ctx.quartzScheduler,ctx)
				}
			}
		}
        // TODO Implement post initialization spring config (optional)
    }

    def onChange = { event ->
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    def onConfigChange = { event ->
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

	//private ConfigObject loadQuartzConfig(config) {
	Properties loadQuartzConfig(config) {
        def properties = new Properties()
        if (config.org.containsKey('quartz')) {
            properties << config.org.quartz.toProperties('org.quartz')
        }

        //config.quartz._properties = properties

        return properties
    }

}
