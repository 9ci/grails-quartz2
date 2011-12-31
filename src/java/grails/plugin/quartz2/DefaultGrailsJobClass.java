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

package grails.plugin.quartz2;

import grails.plugin.quartz2.TriggersBuilder;
import grails.util.GrailsUtil;
import groovy.lang.Closure;
import org.codehaus.groovy.grails.commons.AbstractInjectableGrailsClass;
import org.codehaus.groovy.grails.commons.GrailsClassUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobKey;
import org.quartz.utils.Key;

import java.util.HashMap;
import java.util.Map;

import static grails.plugin.quartz2.GrailsJobClassConstants.*;


/**
 * Grails artefact class which represents a Quartz job.
 *
 * @author Micha?? K??ujszo
 * @author Marcel Overdijk
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.1
 */
public class DefaultGrailsJobClass extends AbstractInjectableGrailsClass implements GrailsJobClass {

    public static final String JOB = "Job";
    private Map triggers = new HashMap();


    public DefaultGrailsJobClass(Class clazz) {
        super(clazz, JOB);
        //evaluateTriggers();
    }

    private void evaluateTriggers() {
        // registering additional triggersClosure from 'triggersClosure' closure if present
        //Closure triggersClosure = (Closure) GrailsClassUtils.getStaticPropertyValue(getClazz(), "triggers");
		Closure triggersClosure = (Closure) getPropertyOrStaticPropertyOrFieldValue("triggers",Closure.class);
        TriggersBuilder builder = new TriggersBuilder(getFullName());

        if (triggersClosure != null) {
            builder.build(triggersClosure);
            triggers = (Map) builder.getTriggers();
        }
    }

    public void execute() {
        getMetaClass().invokeMethod(getReferenceInstance(), "execute", new Object[]{});
    }

    public void execute(JobExecutionContext context) {
        getMetaClass().invokeMethod(getReferenceInstance(), "execute", new Object[]{context});
    }

    public String getGroup() {
        String group = (String) getPropertyOrStaticPropertyOrFieldValue("group", String.class);
        if (group == null || "".equals(group)) return DEFAULT_GROUP;
        return group;
    }

    public boolean isConcurrent() {
        Boolean concurrent = (Boolean) getPropertyValue(CONCURRENT, Boolean.class);
        return concurrent == null ? true : concurrent;
    }

    public boolean isSessionRequired() {
        Boolean sessionRequired = (Boolean) getPropertyValue(SESSION_REQUIRED, Boolean.class);
        return sessionRequired == null ? true : sessionRequired;
    }

	public boolean isGormSession() {
        Boolean sessionRequired = (Boolean) getPropertyValue("gormSession", Boolean.class);
        return sessionRequired == null ? true : sessionRequired;
    }

    public boolean getDurability() {
        Boolean durability = (Boolean) getPropertyValue(DURABILITY, Boolean.class);
        return durability == null ? true : durability;
    }

    public boolean getRequestsRecovery() {
        Boolean requestsRecovery = (Boolean) getPropertyValue(REQUESTS_RECOVERY, Boolean.class);
        return requestsRecovery == null ? false : requestsRecovery;
    }

    public Map getTriggers() {
		if(triggers.isEmpty()){
			evaluateTriggers();
		}
        return triggers;
    }

	
	public JobKey getJobKey() {
		String group = (String) getPropertyOrStaticPropertyOrFieldValue("group", String.class);
		if (group == null || "".equals(group)) group = DEFAULT_GROUP;
		return new JobKey(getFullName(),group);
    }
}
