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

package grails.plugin.quartz2

import grails.plugin.quartz2.GrailsJobClassConstants as Constants

import grails.util.GrailsUtil
import org.quartz.CronExpression
import org.quartz.CronTrigger
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.JobKey
import org.quartz.TriggerKey
import org.quartz.utils.Key
import org.quartz.impl.triggers.CronTriggerImpl
import org.quartz.impl.triggers.SimpleTriggerImpl

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

import java.beans.PropertyEditorSupport;
import java.text.ParseException;

/**
 * Groovy Builder for parsing triggers configuration info.
 */
public class TriggerHelper{
	
	static TriggerKey generateTriggerKey(){
		 new TriggerKey("GRAILS_${Key.createUniqueName(null)}", null)
    }

	/**
	 * @param jobKey - the key for the job to schedule againsts
	 * @param startTime - A Date set to the time for the Trigger to fire.
	 * @param repeatCount - The number of times for the Trigger to repeat firing, use SimpleTrigger.REPEAT_INDEFINITELYfor unlimitted times.
	 * @param interval - The number of milliseconds to pause between the repeat firing.
	 */
	static SimpleTriggerImpl simpleTrigger(JobKey jobKey, Date startTime, int repeatCount, Long interval, Map params = null){
		def trigger = new SimpleTriggerImpl()
		trigger = commonTriggerSetup(trigger,jobKey,params)
		trigger.startTime = new Date()
		trigger.repeatCount = repeatCount
		trigger.repeatInterval = interval
		return trigger
	}
	
	/**
	 * @param jobKey - the key for the job to schedule againsts
	 * @param cronExpression - the cron expression string
	 * @param params - jobData map
	 */
	static CronTriggerImpl cronTrigger(JobKey jobKey, String cronExpression, Map params = null){
		def trigger = new CronTriggerImpl()
		trigger = commonTriggerSetup(trigger,jobKey,params)
		trigger.cronExpression = cronExpression
		return trigger
	}
	
	/**
	 * @param jobKey - the key for the job to schedule againsts
	 * @param cronExpression - the cron expression string
	 * @param params - jobData map
	 */
	static commonTriggerSetup(trigger, JobKey jobKey, Map params = null){
		trigger.key = new TriggerKey("GRAILS_${Key.createUniqueName(null)}", null)
		trigger.jobKey = jobKey
	    if (params) trigger.jobDataMap.putAll(params)
		return trigger
	}
	

	
	
	
}
