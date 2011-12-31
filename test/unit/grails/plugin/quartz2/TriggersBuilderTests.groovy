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

/**
 * TODO: write javadoc
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 */
class TriggersBuilderTests extends GroovyTestCase {
    void testConfigBuilder() {
        def builder = new TriggersBuilder('TestJob')
        def closure = {
            simple()
            simple repeatInterval: 1000
            simple startDelay:500
            simple startDelay:500, repeatInterval: 1000
            simple startDelay:500, repeatInterval: 1000, repeatCount: 3
            simple name: 'everySecond', repeatInterval:1000
            cron()
            cron cronExpression:'0 15 6 * * ?'
            cron name: 'myTrigger', cronExpression:'0 15 6 * * ?'
            simple startDelay:500, repeatInterval: 1000, repeatCount: 0
			simple repeatInterval: 1000, misfireInstruction:SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
        }
        builder.build(closure)

        assertEquals 'Invalid triggers count', 11, builder.triggers.size()

        def jobName = 'TestJob0'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: Constants.DEFAULT_START_DELAY,
                repeatInterval: Constants.DEFAULT_REPEAT_INTERVAL,
                repeatCount: SimpleTrigger.REPEAT_INDEFINITELY
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'TestJob1'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: Constants.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: SimpleTrigger.REPEAT_INDEFINITELY
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'TestJob2'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: 500,
                repeatInterval: Constants.DEFAULT_REPEAT_INTERVAL,
                repeatCount: SimpleTrigger.REPEAT_INDEFINITELY
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'TestJob3'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: SimpleTrigger.REPEAT_INDEFINITELY
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'TestJob4'
         
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: 3
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'everySecond'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: Constants.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: SimpleTrigger.REPEAT_INDEFINITELY
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'TestJob5'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay:Constants.DEFAULT_START_DELAY,
                cronExpression: Constants.DEFAULT_CRON_EXPRESSION
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'TestJob6'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                cronExpression: '0 15 6 * * ?',
                startDelay: Constants.DEFAULT_START_DELAY
            ), builder.triggers[jobName].triggerAttributes
        )
        
        jobName = 'myTrigger'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: Constants.DEFAULT_START_DELAY,
                cronExpression: '0 15 6 * * ?'
            ), builder.triggers[jobName].triggerAttributes
        )

        jobName = 'TestJob7'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: 500,
                repeatInterval: 1000,
                repeatCount: 0
            ), builder.triggers[jobName].triggerAttributes
        )

		jobName = 'TestJob8'
        
        assertPropertiesEquals(new Expando(
                name:jobName,
                group: Constants.DEFAULT_TRIGGERS_GROUP,
                startDelay: Constants.DEFAULT_START_DELAY,
                repeatInterval: 1000,
                repeatCount: SimpleTrigger.REPEAT_INDEFINITELY,
				misfireInstruction:SimpleTrigger.MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
            ), builder.triggers[jobName].triggerAttributes
        )
    }

    private assertPropertiesEquals(expected, actual) {
        expected.properties.each { entry ->
            assert actual[entry.key] == entry.value, "Unexpected value for property: ${entry.key}" 
        }
        assert actual.size() == expected.properties?.size()
    }

}
