org{
	quartz{
		scheduler.skipUpdateCheck = true
		scheduler.instanceName = 'DefaultPluginScheduler'
		threadPool.class = 'org.quartz.simpl.SimpleThreadPool'
		threadPool.threadCount = 10
		threadPool.threadPriority = 5
		threadPool.threadsInheritContextClassLoaderOfInitializingThread = true
		jobStore.class = 'org.quartz.simpl.RAMJobStore'
	}
}

grails{
	plugin{
		quartz2 {
    		autoStartup = true
		}
	}
}

environments {
    test {
        grails.plugin.quartz2.autoStartup = false
    }
}
