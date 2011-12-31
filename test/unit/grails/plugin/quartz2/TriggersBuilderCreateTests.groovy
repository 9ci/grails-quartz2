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
import org.quartz.CronExpression
import org.quartz.CronTrigger
import org.quartz.SimpleTrigger
import org.quartz.Trigger
import org.quartz.JobKey
import org.quartz.impl.triggers.CronTriggerImpl
import org.quartz.impl.triggers.SimpleTriggerImpl


class TriggersBuilderCreateTests extends GroovyTestCase {


	void test_simple_triggers_creation(){
		TriggersBuilder builder = new TriggersBuilder("TestSimpleJob");
		def triggersClosure = {
            simple name: 'simpleTrigger', startDelay:500, repeatInterval: 1000, misfireInstruction:SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
        }
		
	    builder.build(triggersClosure)
		
		assert builder.triggers['simpleTrigger']
		def trigger = TriggersBuilder.createTrigger(builder.triggers['simpleTrigger'],null)
		//println "name:${trigger.name} class:${trigger.class}"
		assert trigger instanceof SimpleTriggerImpl
		assert trigger.name == 'simpleTrigger'
		assert trigger.misfireInstruction == SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT

	}
	
	void test_cron_triggers_creation(){
		TriggersBuilder builder = new TriggersBuilder("TestCronJob");
		def startTime = new Date()
		assert startTime instanceof java.util.Date
		def triggersClosure = {
            cron cronExpression:'0 15 6 * * ?'
			cron cronExpression:'0 15 6 * * ?', startTime: startTime, misfireInstruction:CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
        }
		
	    builder.build(triggersClosure)
		
		def trigger = TriggersBuilder.createTrigger(builder.triggers['TestCronJob0'],null)
		//println "name:${trigger.name} class:${trigger.class}"
		assert trigger instanceof CronTriggerImpl
		assert trigger.name == 'TestCronJob0'
		assert trigger.cronExpression == '0 15 6 * * ?'
		
		//assert trigger.misfireInstruction == SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
		//assert trigger.startDelay == Constants.DEFAULT_START_DELAY
		
		trigger = TriggersBuilder.createTrigger(builder.triggers['TestCronJob1'],null)
		assert trigger instanceof CronTriggerImpl
		assert trigger.name == 'TestCronJob1'
		//assert trigger.startTime.compareTo(startTime) == 0
		assert trigger.misfireInstruction == CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING
		

	}
	

}
