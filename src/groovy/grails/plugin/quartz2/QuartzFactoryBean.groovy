/*
 * Copyright 2002-2011 the original author or authors.
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

package grails.plugin.quartz2

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.quartz.impl.StdSchedulerFactory

import org.quartz.Calendar;
import org.quartz.JobDetail;
import org.quartz.JobListener;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerListener;
import org.quartz.Trigger;
import org.quartz.TriggerListener;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.scheduling.SchedulingException;
import org.springframework.util.CollectionUtils;

/**
 * A very simple factory bean for the Quartz Scheduler
 * took a lot of ideas and used Spring's SchedulerFactoryBean as a starting point
 *
 * @author Joshua Burnett
 * @author Juergen Hoeller wrote original SchedulerFactoryBean
 */
public class QuartzFactoryBean implements FactoryBean<Scheduler>, ApplicationContextAware,InitializingBean, DisposableBean, SmartLifecycle {
	private static final transient Log log = LogFactory.getLog(JobErrorLoggerListener.class);
	
	def grailsApplication
	Scheduler scheduler
	boolean autoStartup = true
	ApplicationContext applicationContext
	boolean waitForJobsToCompleteOnShutdown = false
	int phase = Integer.MAX_VALUE
	Properties quartzProperties
	JobListener[] globalJobListeners

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	
	//SmartLifecycle


	//---------------------------------------------------------------------
	// Implementation of InitializingBean interface
	//---------------------------------------------------------------------

	public void afterPropertiesSet() throws Exception {
		println "afterPropertiesSet Quartz Scheduler in QuartzFactoryBean"
		if(quartzProperties){
			StdSchedulerFactory fact = new StdSchedulerFactory();
			fact.initialize(quartzProperties)
			scheduler = fact.getScheduler()
		}else{
			scheduler = StdSchedulerFactory.getDefaultScheduler()
		}
		
		scheduler.getContext().put("applicationContext", this.applicationContext)
		scheduler.getContext().put("grailsApplication", this.grailsApplication)
		if (this.globalJobListeners != null) {
			for (JobListener listener : this.globalJobListeners) {
				scheduler.listenerManager.addJobListener(listener)
			}
		}
	}

	//---------------------------------------------------------------------
	// Implementation of FactoryBean interface
	//---------------------------------------------------------------------

	@Override
	public Scheduler getScheduler() {
		return this.scheduler;
	}

	public Scheduler getObject() {
		return this.scheduler;
	}

	public Class<? extends Scheduler> getObjectType() {
		return (this.scheduler != null) ? this.scheduler.getClass() : Scheduler.class;
	}

	public boolean isSingleton() {
		return true;
	}


	//---------------------------------------------------------------------
	// Implementation of Lifecycle interface
	//---------------------------------------------------------------------

	public void start() throws SchedulingException {
		if (this.scheduler != null) {
			try {
				log.info("Starting Quartz Scheduler in QuartzFactoryBean");
				println "Starting Quartz Scheduler in QuartzFactoryBean"
				scheduler.start();
			}
			catch (SchedulerException ex) {
				throw new SchedulingException("Could not start Quartz Scheduler", ex);
			}
		}
	}

	public void stop() throws SchedulingException {
		if (this.scheduler != null) {
			try {
				this.scheduler.standby();
			}
			catch (SchedulerException ex) {
				throw new SchedulingException("Could not stop Quartz Scheduler", ex);
			}
		}
	}

	public void stop(Runnable callback) throws SchedulingException {
		stop();
		callback.run();
	}

	public boolean isRunning() throws SchedulingException {
		if (this.scheduler != null) {
			try {
				return !this.scheduler.isInStandbyMode();
			}
			catch (SchedulerException ex) {
				return false;
			}
		}
		return false;
	}

	public boolean isAutoStartup() {
		return this.autoStartup;
	}

	//---------------------------------------------------------------------
	// Implementation of DisposableBean interface
	//---------------------------------------------------------------------

	/**
	 * Shut down the Quartz scheduler on bean factory shutdown,
	 * stopping all scheduled jobs.
	 */
	public void destroy() throws SchedulerException {
		log.info("Shutting down Quartz Scheduler");
		this.scheduler.shutdown(this.waitForJobsToCompleteOnShutdown);
	}

}
