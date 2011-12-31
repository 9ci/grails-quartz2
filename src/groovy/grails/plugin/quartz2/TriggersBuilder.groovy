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
import org.quartz.impl.triggers.CronTriggerImpl
import org.quartz.impl.triggers.SimpleTriggerImpl

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.PropertyAccessorFactory;

import java.beans.PropertyEditorSupport;

/**
 * Groovy Builder for parsing triggers configuration info.
 *
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 *
 * @since 0.3
 */
public class TriggersBuilder extends BuilderSupport {
    private triggerNumber = 0
    private jobName

    def triggers = [:]

    public TriggersBuilder(String jobName) {
        super()
        this.jobName = jobName
    }

    public build(closure) {
        closure.delegate = this
        closure.call()
        return triggers
    }

    protected void setParent(parent, child) {}

    protected createNode(name) {
        createNode(name, null, null)
    }

    protected createNode(name, value) {
        createNode(name, null, value)
    }

    protected createNode(name, Map attributes) {
        createNode(name, attributes, null)
    }

    protected Object createNode(name, Map attributes, Object value) {
        def trigger = createTrigger(name, attributes, value)
        triggers[trigger.triggerAttributes.name] = trigger
        trigger
    }

    public Expando createTrigger(name, Map attributes, value) {
        def triggerClass
        def triggerAttributes = attributes ? new HashMap(attributes) : [:]

        prepareCommonTriggerAttributes(triggerAttributes)

        def triggerType = name

        switch (triggerType) {
            case 'simple':
                triggerClass = SimpleTriggerImpl
                prepareSimpleTriggerAttributes(triggerAttributes)
                break
            case 'cron':
                triggerClass = CronTriggerImpl
                prepareCronTriggerAttributes(triggerAttributes)
                break
            case 'custom':
                if (!triggerAttributes?.triggerClass) throw new Exception("Custom trigger must have 'triggerClass' attribute")
                triggerClass = (Class) triggerAttributes.remove('triggerClass')
                if (!Trigger.isAssignableFrom(triggerClass)) throw new Exception("Custom trigger class must extend org.quartz.Trigger class.")
                break
            default:
                throw new Exception("Invalid format")
        }

        new Expando(triggerClass: triggerClass, triggerAttributes: triggerAttributes)
    }

    private prepareCommonTriggerAttributes(HashMap triggerAttributes) {
        if (triggerAttributes['name'] == null) triggerAttributes['name'] = "${jobName}${triggerNumber++}"
        if (triggerAttributes['group'] == null) triggerAttributes['group'] = Constants.DEFAULT_TRIGGERS_GROUP
		//if (triggerAttributes[Constants.JOBKEY] == null) triggerAttributes[Constants.JOBKEY] = new TriggerKey(triggerAttributes[Constants.NAME] )
        
		if (triggerAttributes[Constants.START_DELAY] == null) triggerAttributes[Constants.START_DELAY] = Constants.DEFAULT_START_DELAY
        if (!(triggerAttributes[Constants.START_DELAY] instanceof Integer || triggerAttributes[Constants.START_DELAY] instanceof Long)) {
            throw new IllegalArgumentException("startDelay trigger property in the job class ${jobName} must be Integer or Long");
        }
        if (((Number) triggerAttributes[Constants.START_DELAY]).longValue() < 0) {
            throw new IllegalArgumentException("startDelay trigger property in the job class ${jobName} is negative (possibly integer overflow error)");
        }
    }

    private def prepareSimpleTriggerAttributes(HashMap triggerAttributes) {
        if (triggerAttributes[Constants.REPEAT_INTERVAL] == null) triggerAttributes[Constants.REPEAT_INTERVAL] = Constants.DEFAULT_REPEAT_INTERVAL
        if (!(triggerAttributes[Constants.REPEAT_INTERVAL] instanceof Integer || triggerAttributes[Constants.REPEAT_INTERVAL] instanceof Long)) {
            throw new IllegalArgumentException("repeatInterval trigger property in the job class ${jobName} must be Integer or Long");
        }
        if (((Number) triggerAttributes[Constants.REPEAT_INTERVAL]).longValue() < 0) {
            throw new IllegalArgumentException("repeatInterval trigger property for job class ${jobName} is negative (possibly integer overflow error)");
        }
        if (triggerAttributes[Constants.REPEAT_COUNT] == null) triggerAttributes[Constants.REPEAT_COUNT] = SimpleTrigger.REPEAT_INDEFINITELY
        if (!(triggerAttributes[Constants.REPEAT_COUNT] instanceof Integer || triggerAttributes[Constants.REPEAT_COUNT] instanceof Long)) {
            throw new IllegalArgumentException("repeatCount trigger property in the job class ${jobName} must be Integer or Long");
        }
        if (((Number) triggerAttributes[Constants.REPEAT_COUNT]).longValue() < 0 && ((Number) triggerAttributes[Constants.REPEAT_COUNT]).longValue() != SimpleTrigger.REPEAT_INDEFINITELY) {
            throw new IllegalArgumentException("repeatCount trigger property for job class ${jobName} is negative (possibly integer overflow error)");
        }
    }

    private def prepareCronTriggerAttributes(HashMap triggerAttributes) {
        if (!triggerAttributes?.cronExpression) triggerAttributes[Constants.CRON_EXPRESSION] = Constants.DEFAULT_CRON_EXPRESSION
        if (!CronExpression.isValidExpression(triggerAttributes[Constants.CRON_EXPRESSION].toString())) {
            throw new IllegalArgumentException("Cron expression '${triggerAttributes[Constants.CRON_EXPRESSION]}' in the job class ${jobName} is not a valid cron expression");
        }
    }


    public Map createEmbeddedSimpleTrigger(startDelay, timeout, repeatCount) {
        return [(jobName): createTrigger('simple', [name: jobName, startDelay: startDelay, repeatInterval: timeout, repeatCount: repeatCount], null)]
    }

    public Map createEmbeddedCronTrigger(startDelay, cronExpression) {
        return [(jobName): createTrigger('cron', [name: jobName, startDelay: startDelay, cronExpression: cronExpression], null)]
    }

	static def createTrigger(trigger, jobKey){
		def triggerInstance = BeanUtils.instantiateClass(trigger.triggerClass)

        if (trigger.triggerAttributes.containsKey(GrailsJobClassConstants.START_DELAY)) {
            Number startDelay = (Number) trigger.triggerAttributes.remove(GrailsJobClassConstants.START_DELAY)
            triggerInstance.setStartTime(new Date(System.currentTimeMillis() + startDelay.longValue()))
        }

        if (jobKey) {
			triggerInstance.jobKey = jobKey
        }

        BeanWrapper customTriggerWrapper = PropertyAccessorFactory.forBeanPropertyAccess(triggerInstance)
        customTriggerWrapper.registerCustomEditor(String.class, new StringEditor())
        customTriggerWrapper.setPropertyValues(trigger.triggerAttributes)
		//println "misfire is $triggerInstance.misfireInstruction"
		return triggerInstance
	}
	

}

// We need this additional editor to support GString -> String convertion for trigger's properties.
class StringEditor extends PropertyEditorSupport {
    @Override
    public void setValue(Object value) {
        super.setValue(value == null ? null : value.toString());
    }

    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        setValue(text);
    }
}
