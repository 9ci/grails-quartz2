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

import org.codehaus.groovy.grails.commons.InjectableGrailsClass;

import java.util.Map;
import org.quartz.JobKey;

/**
 * Represents a job class in Grails.
 *
 * @author Micha?? K??ujszo
 * @author Graeme Rocher
 * @author Marcel Overdijk
 * @author Sergey Nebolsin (nebolsin@gmail.com)
 * @since 0.1
 */
public interface GrailsJobClass extends InjectableGrailsClass {
	
    /**
     * Method which is executed by the job scheduler.
     */
    public void execute();

    /**
     * Get group name used for configuring scheduler.
     *
     * @return jobs group name for this job
     */
    public String getGroup();

	/**
     * gets the JobKey for this Job
     *
     * @return JobKey
     */
	public JobKey getJobKey();

    /**
     * If jobs can be executed concurrently returns true.
     *
     * @return true if several instances of this job can run concurrently
     */
    public boolean isConcurrent();

    /**
     * If job requires Hibernate Session bounded to thread returns true.
     *
     * @return true if this job require a Hibernate Session bounded to thread
     */
    public boolean isSessionRequired();

	public boolean isGormSession();

    /**
     * If job is durable returns true.
     *
     * @return true if this job is durable
     */
    public boolean getDurability();

    /**
     * If job should be re-executed if a 'recovery' or 'fail-over' situation is encountered returns true.
     *
     * @return true if this job requests recovery
     */
    public boolean getRequestsRecovery();

    public Map getTriggers();
}
